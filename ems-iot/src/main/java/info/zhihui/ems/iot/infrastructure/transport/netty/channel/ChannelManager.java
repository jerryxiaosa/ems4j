package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.util.HexUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Component
@Slf4j
public class ChannelManager {

    // ===== 命令队列与超时配置 =====
    // 单通道待发送队列上限，防止请求无界堆积带来内存压力。
    private static final int MAX_QUEUE_SIZE = 5;
    // 等待设备 ACK 的超时时间（毫秒）；超时后会失败当前命令并关闭通道。
    private static final long COMMAND_TIMEOUT_MILLIS = 15_000L;
    // 跨线程投递到 EventLoop 后，调用方等待执行结果的最长时间（毫秒）。
    private static final long EVENT_LOOP_WAIT_TIMEOUT_MILLIS = 3_000L;

    // ===== 异常频次控制配置 =====
    // 异常统计窗口（毫秒），用于限制短时间异常风暴。
    private static final long ABNORMAL_WINDOW_MILLIS = 30_000L;
    // 异常统计阈值：窗口期内超过该次数即判定为异常过载。
    private static final int ABNORMAL_MAX_COUNT = 5;

    // channelId -> ChannelSession，维护通道维度的会话状态。
    private final Map<String, ChannelSession> sessions = new ConcurrentHashMap<>();
    // deviceNo -> channelId，用于按设备快速定位当前绑定通道。
    private final Map<String, String> deviceNoToChannelId = new ConcurrentHashMap<>();

    /**
     * 注册通道与设备的绑定关系，并初始化对应的队列/状态。
     *
     * @param session 通道与设备关联信息
     * @throws IllegalArgumentException session、channel或deviceNo为空
     */
    public void register(ChannelSession session) {
        executeInEventLoop(session, () -> registerInLoop(session));
    }

    private void registerInLoop(ChannelSession session) {
        if (StringUtils.isBlank(session.getDeviceNo())) {
            throw new IllegalArgumentException("session.deviceNo 不能为空");
        }

        String channelId = session.getChannel().id().asLongText();
        log.debug("开始注册通道 {} 到设备No {} 类型 {}", channelId, session.getDeviceNo(), session.getDeviceType());
        ChannelSession existing = sessions.get(channelId);
        // 如果已经存在，替换信息
        if (existing != null) {
            log.debug("通道 {} 已存在，更新信息", channelId);
            String oldDeviceNo = existing.getDeviceNo();
            String newDeviceNo = session.getDeviceNo();
            if (StringUtils.isNotBlank(oldDeviceNo) && !oldDeviceNo.equals(newDeviceNo)) {
                deviceNoToChannelId.remove(oldDeviceNo, channelId);
            }
            existing.setDeviceNo(newDeviceNo)
                    .setDeviceType(session.getDeviceType())
                    .setChannel(session.getChannel());
            session = existing;
        }
        sessions.put(channelId, session);
        String deviceNo = session.getDeviceNo();
        if (StringUtils.isNotBlank(deviceNo)) {
            String oldChannelId = deviceNoToChannelId.put(deviceNo, channelId);
            // 原来就已经存在，并且channelId不一致
            if (oldChannelId != null && !oldChannelId.equals(channelId)) {
                // 设备已重绑到新通道，直接在当前流程中关闭并移除旧通道，避免旧映射继续生效。
                // 注意！这里两个eventLoop里的操作
                closeAndRemoveInLoop(oldChannelId);
            }
        }
        log.info("绑定通道 {} 到设备No {} 类型 {}", channelId, session.getDeviceNo(), session.getDeviceType());
    }

    /**
     * 移除通道绑定
     * 清空队列与挂起等待，防止资源泄漏。
     *
     * @param channelId 通道ID
     */
    public void remove(String channelId) {
        if (StringUtils.isBlank(channelId)) {
            return;
        }
        ChannelSession session = sessions.get(channelId);
        if (session == null) {
            log.info("通道 {} ，绑定已被移除", channelId);
            return;
        }
        executeInEventLoop(session, () -> removeInLoop(channelId));
    }

    private void removeInLoop(String channelId) {
        ChannelSession session = sessions.remove(channelId);
        if (session != null) {
            String deviceNo = session.getDeviceNo();
            if (StringUtils.isNotBlank(deviceNo)) {
                deviceNoToChannelId.remove(deviceNo, channelId);
            }

            // 清空队列，避免潜在内存泄漏；未完成任务直接异常完成
            Queue<PendingTask> queue = session.getQueue();
            PendingTask queuedTask;
            while ((queuedTask = queue.poll()) != null) {
                queuedTask.future().completeExceptionally(new IllegalStateException("通道已移除"));
            }

            // 正在执行的任务异常完成
            CompletableFuture<byte[]> pendingFuture = session.getPendingFuture();
            if (pendingFuture != null) {
                pendingFuture.completeExceptionally(new IllegalStateException("通道已移除"));
                session.setPendingFuture(null);
            }
        }
        log.info("已移除通道 {} 的绑定", channelId);
    }

    /**
     * 记录一次异常事件，并返回是否超过阈值。
     *
     * @param channelId 通道ID
     * @param reason    异常原因
     * @param nowMillis 当前时间戳
     * @return 是否超过阈值
     */
    public boolean recordAbnormal(String channelId, AbnormalReasonEnum reason, long nowMillis) {
        ChannelSession session = sessions.get(channelId);
        if (session == null) {
            log.warn("异常统计失败，通道 {} 不存在，原因 {}", channelId, reason);
            return false;
        }
        Deque<Long> timestamps = session.getAbnormalTimestamps();
        timestamps.addLast(nowMillis);
        while (!timestamps.isEmpty() && nowMillis - timestamps.peekFirst() > ABNORMAL_WINDOW_MILLIS) {
            timestamps.removeFirst();
        }

        boolean exceeded = timestamps.size() > ABNORMAL_MAX_COUNT;
        if (exceeded) {
            log.warn("通道 {} 在 {}ms 内异常次数 {} 超过阈值 {}, 最新原因 {}",
                    channelId, ABNORMAL_WINDOW_MILLIS, timestamps.size(), ABNORMAL_MAX_COUNT, reason);
        }
        return exceeded;
    }

    /**
     * 发送并等待设备响应（需要协议 handler 调用 completePending）
     *
     * @param deviceNo 设备No
     * @param message  消息
     * @return 响应数据
     * @throws IllegalArgumentException deviceNo或message为空
     */
    public CompletableFuture<byte[]> sendInQueue(String deviceNo, Object message) {
        return enqueue(deviceNo, message, true);
    }

    /**
     * 发送但不等待设备响应（写成功即视为完成）
     *
     * @param deviceNo 设备No
     * @param message  消息
     */
    public void sendInQueueWithoutWaiting(String deviceNo, Object message) {
        CompletableFuture<byte[]> future = enqueue(deviceNo, message, false);
        future.whenComplete((payload, ex) -> {
            if (ex != null) {
                log.warn("通道发送失败（不等待响应），deviceNo={}", deviceNo, ex);
            }
        });

    }

    /**
     * 发送数据（不进行等待）
     * 注意！这里没有转换到eventLoop上处理
     * 但是仅仅做了writeAndFlush 如果后面要加入更多功能会有风险
     *
     * @param channelId 通道ID
     * @param payload   数据
     */
    public void sendDirectly(String channelId, byte[] payload) {
        ChannelSession session = sessions.get(channelId);

        if (session != null && session.getChannel() != null && session.getChannel().isActive() && payload != null) {
            session.getChannel().writeAndFlush(Unpooled.wrappedBuffer(payload));
            log.debug("通道 {} 直接发送数据 {}", channelId, HexUtil.bytesToHexString(payload));
        }
    }

    public void closeAndRemove(String channelId) {
        if (StringUtils.isBlank(channelId)) {
            return;
        }
        ChannelSession session = sessions.get(channelId);
        if (session == null) {
            remove(channelId);
            return;
        }
        executeInEventLoop(session, () -> closeAndRemoveInLoop(channelId));
    }

    private void closeAndRemoveInLoop(String channelId) {
        ChannelSession oldSession = sessions.get(channelId);
        removeInLoop(channelId);
        if (oldSession != null && oldSession.getChannel() != null && oldSession.getChannel().isActive()) {
            oldSession.getChannel().close();
        }
    }

    private CompletableFuture<byte[]> enqueue(String deviceNo, Object message, boolean requireAck) {
        if (StringUtils.isBlank(deviceNo)) {
            throw new IllegalArgumentException("deviceNo 不能为空");
        }

        ChannelSession session = getSessionByDeviceNo(deviceNo);
        // 连接出现问题，那么抛出异常不会有更严重影响
        if (session == null) {
            throw new IllegalStateException("deviceNo " + deviceNo + " 未找到通道");
        }
        if (session.getChannel() == null || !session.getChannel().isActive()) {
            throw new IllegalStateException("deviceNo " + deviceNo + " 通道不活跃");
        }
        return callInEventLoop(session, () -> enqueueInLoop(session, deviceNo, message, requireAck));
    }

    private CompletableFuture<byte[]> enqueueInLoop(ChannelSession session, String deviceNo, Object message,
                                                    boolean requireAck) {
        if (session.getChannel() == null || !session.getChannel().isActive()) {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("deviceNo " + deviceNo + " 通道不活跃"));
            return future;
        }

        String channelId = session.getChannel().id().asLongText();
        Queue<PendingTask> queue = session.getQueue();
        if (queue.size() >= MAX_QUEUE_SIZE) {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("通道 " + channelId + " 队列已满"));
            return future;
        }

        CompletableFuture<byte[]> future = new CompletableFuture<>();
        queue.offer(new PendingTask(deviceNo, message, future, requireAck));
        tryDispatchInLoop(session);
        return future;
    }

    private void tryDispatchInLoop(ChannelSession session) {
        Queue<PendingTask> queue = session != null ? session.getQueue() : null;
        if (queue == null) {
            return;
        }

        // 未在发送时才取队列
        if (session.getSending().compareAndSet(false, true)) {
            PendingTask task = queue.poll();
            if (task == null) {
                session.getSending().set(false);
                return;
            }
            sendTaskInLoop(session, task);
        }
    }

    private void sendTaskInLoop(ChannelSession session, PendingTask task) {
        if (session == null || session.getChannel() == null || !session.getChannel().isActive()) {
            task.future().completeExceptionally(new IllegalStateException("通道不可用"));

            // 让后续任务也快速以“通道不可用”异常完成
            if (session != null && session.getChannel() != null) {
                session.getSending().set(false);
                tryDispatchInLoop(session);
            }
            return;
        }
        if (task.requireAck()) {
            session.setPendingFuture(task.future());
            schedulePendingTimeoutInLoop(session, task.deviceNo(), task.future());
            session.getChannel().writeAndFlush(task.packet()).addListener((ChannelFutureListener) writeFuture -> {
                if (!writeFuture.isSuccess()) {
                    failPendingInLoop(session, task.deviceNo(), writeFuture.cause(), task.future());
                }
            });
            return;
        }

        session.getChannel().writeAndFlush(task.packet()).addListener((ChannelFutureListener) writeFuture -> {
            if (!writeFuture.isSuccess()) {
                task.future().completeExceptionally(writeFuture.cause());
            } else {
                task.future().complete(new byte[0]);
            }
            session.getSending().set(false);
            tryDispatchInLoop(session);
        });
    }

    /**
     * 完成等待的任务
     * 由协议 handler 在识别到响应帧时调用，完成等待的 Future。
     *
     * @param deviceNo 设备No
     * @param payload  响应数据
     * @return 是否完成
     * @throws IllegalArgumentException deviceNo为空
     * @throws IllegalStateException    未找到会话或无挂起任务
     */
    public boolean completePending(String deviceNo, byte[] payload) {
        if (StringUtils.isBlank(deviceNo)) {
            log.warn("完成下行响应失败：deviceNo为空");
            throw new IllegalArgumentException("deviceNo 不能为空");
        }
        ChannelSession session = getSessionByDeviceNo(deviceNo);
        if (session == null) {
            log.warn("完成下行响应失败：未找到会话，deviceNo={}", deviceNo);
            throw new IllegalStateException("deviceNo " + deviceNo + " 未找到会话");
        }
        return callInEventLoop(session, () -> completePendingInLoop(session, deviceNo, payload));
    }

    private boolean completePendingInLoop(ChannelSession session, String deviceNo, byte[] payload) {
        CompletableFuture<byte[]> future = session.getPendingFuture();
        if (future == null) {
            log.warn("完成下行响应失败：无挂起任务，deviceNo={} channelId={} ",
                    deviceNo, session.getChannel() == null ? null : session.getChannel().id().asLongText());
            throw new IllegalStateException("deviceNo " + deviceNo + " 无挂起任务");
        }
        session.setPendingFuture(null);
        future.complete(payload);
        session.getSending().set(false);
        tryDispatchInLoop(session);
        return true;
    }

    /**
     * 为当前挂起命令注册超时任务，超时后关闭通道并清理队列。
     */
    private void schedulePendingTimeoutInLoop(ChannelSession session, String deviceNo, CompletableFuture<byte[]> future) {
        if (session.getChannel() == null) {
            return;
        }
        session.getChannel().eventLoop().schedule(
                () -> failPendingInLoop(session, deviceNo, new TimeoutException("命令等待响应超时"), future),
                COMMAND_TIMEOUT_MILLIS,
                TimeUnit.MILLISECONDS
        );
    }

    private void failPendingInLoop(ChannelSession session, String deviceNo, Throwable ex, CompletableFuture<byte[]> expectedFuture) {
        CompletableFuture<byte[]> currentFuture = session.getPendingFuture();
        if (currentFuture == null) {
            return;
        }
        if (expectedFuture != null && currentFuture != expectedFuture) {
            return;
        }
        session.setPendingFuture(null);
        currentFuture.completeExceptionally(ex);
        session.getSending().set(false);
        if (ex instanceof TimeoutException) {
            String channelId = session.getChannel() == null ? null : session.getChannel().id().asLongText();
            log.warn("下行命令超时，关闭通道并清理队列，deviceNo={} channelId={}", deviceNo, channelId);
            if (channelId != null) {
                closeAndRemoveInLoop(channelId);
            }
            return;
        }
        tryDispatchInLoop(session);
    }

    private ChannelSession getSessionByDeviceNo(String deviceNo) {
        String channelId = deviceNoToChannelId.get(deviceNo);
        if (channelId != null) {
            ChannelSession session = sessions.get(channelId);

            if (session != null) {
                // 映射命中后需校验设备号一致，避免脏映射把命令路由到其他设备会话。
                if (deviceNo.equals(session.getDeviceNo()) && session.getChannel() != null) {
                    return session;
                }
                deviceNoToChannelId.remove(deviceNo, channelId);
            } else {
                // 说明deviceNoToChannelId找到的数据不对，需要删除
                deviceNoToChannelId.remove(deviceNo, channelId);
            }
        }

        // 通过deviceNoToChannelId没有找到，那么就一个个的去找
        for (ChannelSession session : sessions.values()) {
            if (deviceNo.equals(session.getDeviceNo()) && session.getChannel() != null) {
                deviceNoToChannelId.put(deviceNo, session.getChannel().id().asLongText());
                return session;
            }
        }
        return null;
    }

    /**
     * 在会话所属 EventLoop 中执行无返回值任务。
     * <p>
     * 该方法是 {@link #callInEventLoop(ChannelSession, Supplier)} 的便捷封装，
     * 用于只需要串行执行、不关心返回值的场景。
     *
     * @param session 会话（用于定位 channel/eventLoop）
     * @param task    待执行任务
     */
    private void executeInEventLoop(ChannelSession session, Runnable task) {
        callInEventLoop(session, () -> {
            task.run();
            // 包装无返回值的任务，统一返回null
            return null;
        });
    }

    /**
     * 在指定会话所属的 EventLoop 中执行并返回结果。
     * <p>
     * 语义：
     * 1) 若当前线程已在 EventLoop 中，则直接执行，避免额外调度开销；
     * 2) 若当前线程不在 EventLoop 中，则投递到 EventLoop，并等待执行结果返回。
     * <p>
     * 注意：该方法可能阻塞当前线程，等待 EventLoop 执行完成。
     *
     * @param session  会话（用于定位 channel/eventLoop）
     * @param supplier 待执行逻辑
     * @param <T>      返回值类型
     * @return 执行结果
     */
    private <T> T callInEventLoop(ChannelSession session, Supplier<T> supplier) {
        if (session == null || session.getChannel() == null) {
            throw new IllegalStateException("通道会话不存在");
        }
        EventLoop eventLoop = session.getChannel().eventLoop();
        // 如果本身就在netty线程，那么直接处执行
        if (eventLoop.inEventLoop()) {
            return supplier.get();
        }
        // 注意这里的签名是CompletableFuture<T>，实际上是一个结果容器
        CompletableFuture<T> resultFuture = new CompletableFuture<>();
        eventLoop.execute(() -> {
            try {
                resultFuture.complete(supplier.get());
            } catch (Throwable ex) {
                resultFuture.completeExceptionally(ex);
            }
        });
        // 同步等待结果容器返回T，T可能是真正的Future
        return awaitEventLoopResult(resultFuture);
    }

    /**
     * 等待 EventLoop 任务执行结果，并将底层异常统一转换为业务可感知异常。
     * 作用是把结果返回给工作线程
     *
     * <p>
     * 异常语义：
     * 1) 线程中断：恢复中断标记并抛出 IllegalStateException；
     * 2) 等待超时：抛出 IllegalStateException；
     * 3) EventLoop 执行异常：若为 RuntimeException 直接透传，否则包装抛出。
     *
     * @param resultFuture EventLoop 执行结果
     * @param <T>          返回值类型
     * @return 执行结果
     */
    private <T> T awaitEventLoopResult(CompletableFuture<T> resultFuture) {
        try {
            return resultFuture.get(EVENT_LOOP_WAIT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待 EventLoop 执行被中断", ex);
        } catch (TimeoutException ex) {
            throw new IllegalStateException("等待 EventLoop 执行超时", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("EventLoop 执行失败", cause);
        }
    }
}
