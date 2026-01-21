package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import io.netty.channel.ChannelFutureListener;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChannelManager {

    private static final int MAX_QUEUE_SIZE = 5;
    private static final long ABNORMAL_WINDOW_MILLIS = 30_000L;
    private static final int ABNORMAL_MAX_COUNT = 5;
    private final Map<String, ChannelSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> deviceNoToChannelId = new ConcurrentHashMap<>();

    /**
     * 注册通道与设备的绑定关系，并初始化对应的队列/状态。
     *
     * @param session 通道与设备关联信息
     */
    public void register(ChannelSession session) {
        String channelId = session.getChannel().id().asLongText();
        ChannelSession existing = sessions.get(channelId);
        // 如果已经存在，替换信息
        if (existing != null) {
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
                ChannelSession oldSession = sessions.get(oldChannelId);
                remove(oldChannelId);
                if (oldSession != null && oldSession.getChannel() != null && oldSession.getChannel().isActive()) {
                    oldSession.getChannel().close();
                }
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
        ChannelSession session = sessions.remove(channelId);
        if (session != null) {
            String deviceNo = session.getDeviceNo();
            if (StringUtils.isNotBlank(deviceNo)) {
                deviceNoToChannelId.remove(deviceNo, channelId);
            }

            // 清空队列，避免潜在内存泄漏；未完成任务直接异常完成
            session.getQueue().forEach(task -> task.future().completeExceptionally(
                    new IllegalStateException("通道已移除")));
            session.getQueue().clear();

            // 正在执行的任务异常完成
            CompletableFuture<byte[]> pendingFuture = session.getPendingFuture();
            if (pendingFuture != null) {
                pendingFuture.completeExceptionally(new IllegalStateException("通道已移除"));
                session.setPendingFuture(null);
            }
        }
        log.info("移除通道 {} 的绑定", channelId);
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
     */
    public CompletableFuture<byte[]> sendWithAck(String deviceNo, Object message) {
        return enqueue(deviceNo, message, true);
    }

    /**
     * 发送但不等待设备响应（写成功即视为完成）
     *
     * @param deviceNo 设备No
     * @param message  消息
     */
    public void sendFireAndForget(String deviceNo, Object message) {
        enqueue(deviceNo, message, false);
    }

    private CompletableFuture<byte[]> enqueue(String deviceNo, Object message, boolean requireAck) {
        ChannelSession session = getSessionByDeviceNo(deviceNo);
        // 连接出现问题，那么抛出异常不会有更严重影响
        if (session == null) {
            throw new IllegalStateException("deviceNo " + deviceNo + " 未找到通道");
        }
        if (session.getChannel() == null || !session.getChannel().isActive()) {
            throw new IllegalStateException("deviceNo " + deviceNo + " 通道不活跃");
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
        tryDispatch(channelId);
        return future;
    }

    private void tryDispatch(String channelId) {
        ChannelSession session = sessions.get(channelId);
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
            sendTask(session, task);
        }
    }

    private void sendTask(ChannelSession session, PendingTask task) {
        if (session == null || session.getChannel() == null || !session.getChannel().isActive()) {
            task.future().completeExceptionally(new IllegalStateException("通道不可用"));

            // 让后续任务也快速以“通道不可用”异常完成
            if (session != null && session.getChannel() != null) {
                session.getSending().set(false);
                tryDispatch(session.getChannel().id().asLongText());
            }
            return;
        }
        if (task.requireAck()) {
            session.setPendingFuture(task.future());
            session.getChannel().writeAndFlush(task.packet()).addListener((ChannelFutureListener) writeFuture -> {
                if (!writeFuture.isSuccess()) {
                    failPending(task.deviceNo(), writeFuture.cause());
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
            tryDispatch(session.getChannel().id().asLongText());
        });
    }

    /**
     * 完成等待的任务
     * 由协议 handler 在识别到响应帧时调用，完成等待的 Future。
     *
     * @param deviceNo 设备No
     * @param payload  响应数据
     * @return 是否完成
     */
    public boolean completePending(String deviceNo, byte[] payload) {
        ChannelSession session = getSessionByDeviceNo(deviceNo);
        if (session == null) {
            return false;
        }
        CompletableFuture<byte[]> future = session.getPendingFuture();
        if (future == null) {
            return false;
        }
        session.setPendingFuture(null);
        future.complete(payload);
        session.getSending().set(false);
        if (session.getChannel() != null) {
            tryDispatch(session.getChannel().id().asLongText());
        }
        return true;
    }

    /**
     * 异常结束等待：超时/通道关闭/写失败时调用，避免 Future 悬挂。
     */
    public void failPending(String deviceNo, Throwable ex) {
        ChannelSession session = getSessionByDeviceNo(deviceNo);
        if (session == null) {
            return;
        }
        CompletableFuture<byte[]> future = session.getPendingFuture();
        if (future == null) {
            return;
        }
        session.setPendingFuture(null);
        future.completeExceptionally(ex);
        session.getSending().set(false);
        if (session.getChannel() != null) {
            tryDispatch(session.getChannel().id().asLongText());
        }
    }

    private ChannelSession getSessionByDeviceNo(String deviceNo) {
        String channelId = deviceNoToChannelId.get(deviceNo);
        if (channelId != null) {
            ChannelSession session = sessions.get(channelId);

            if (session != null) {
                return session;
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
}
