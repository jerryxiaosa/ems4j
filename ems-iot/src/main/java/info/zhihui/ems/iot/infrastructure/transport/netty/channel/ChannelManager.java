package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import info.zhihui.ems.iot.config.ChannelManagerProperties;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.util.HexUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.FastThreadLocalThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Component
@Slf4j
public class ChannelManager {

    // channelId -> ChannelSession，维护通道维度的会话状态。
    private final Map<String, ChannelSession> sessions = new ConcurrentHashMap<>();
    // deviceNo -> channelId，用于按设备快速定位当前绑定通道。
    private final Map<String, String> deviceNoToChannelId = new ConcurrentHashMap<>();
    // 配置信息
    private final ChannelManagerProperties properties;

    public ChannelManager(ChannelManagerProperties properties) {
        this.properties = properties;
    }

    /**
     * 注册通道与设备的绑定关系，并初始化对应的队列/状态。
     *
     * @param session 通道与设备关联信息
     * @throws IllegalArgumentException session、channel或deviceNo为空
     */
    public void register(ChannelSession session) {
        executeSyncInEventLoop(session, () -> {
            registerSessionInLoop(session);
            return null;
        });
    }

    /**
     * 在会话所属 EventLoop 中完成注册逻辑，并在设备重连时踢掉旧连接。
     */
    private void registerSessionInLoop(ChannelSession session) {
        assertInEventLoop(session, "注册通道");
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

        ChannelSession oldSession = findSessionByDeviceNo(session.getDeviceNo());
        // @TODO 当前重绑仍是“先发布新 deviceNo 路由，再异步关闭旧连接”。
        // 若旧会话所在 EventLoop 长时间阻塞，旧 pending / pendingTimeout 可能无法按
        // commandTimeoutMillis 及时收口；后续需要把“旧状态退休”和“旧通道关闭”拆开处理，
        // 并在新映射发布前完成旧状态摘除。
        bindSessionIndexes(session, oldSession);
        if (shouldClosePreviousSession(session, oldSession)) {
            executeAsyncInEventLoop(oldSession, () -> closeSessionInLoop(oldSession));
        }
        log.info("绑定通道 {} 到设备No {} 类型 {}", channelId, session.getDeviceNo(), session.getDeviceType());
    }

    /**
     * 判断当前重绑是否需要清理旧会话；相同对象或相同 channel 的更新不触发重绑关闭。
     */
    private boolean shouldClosePreviousSession(ChannelSession newSession, ChannelSession oldSession) {
        if (oldSession == null || oldSession == newSession) {
            return false;
        }
        return !Objects.equals(channelId(oldSession), channelId(newSession));
    }

    /**
     * 同步维护 channelId 和 deviceNo 两张索引。
     * 若覆盖的旧 channelId 不是本次预期替换的旧会话，则说明存在并发竞争留下的僵尸会话，需要兜底清理。
     */
    private void bindSessionIndexes(ChannelSession session, ChannelSession expectedOldSession) {
        String channelId = session.getChannel().id().asLongText();
        sessions.put(channelId, session);

        String deviceNo = session.getDeviceNo();
        String oldChannelId = deviceNoToChannelId.put(deviceNo, channelId);
        if (StringUtils.isBlank(oldChannelId) || oldChannelId.equals(channelId)) {
            return;
        }

        String expectedOldChannelId = channelId(expectedOldSession);
        if (Objects.equals(oldChannelId, expectedOldChannelId)) {
            return;
        }
        cleanupOrphanedSession(oldChannelId, deviceNo);
    }

    /**
     * 异步清理并发注册竞争留下的僵尸会话。
     */
    private void cleanupOrphanedSession(String orphanedChannelId, String deviceNo) {
        ChannelSession orphanedSession = findSessionByChannelId(orphanedChannelId);
        if (orphanedSession == null) {
            return;
        }
        log.warn("检测到并发注册覆盖，清理僵尸会话 channelId={} deviceNo={}", orphanedChannelId, deviceNo);
        executeAsyncInEventLoop(orphanedSession, () -> closeSessionInLoop(orphanedSession));
    }

    /**
     * 从 deviceNo 索引中解绑当前会话，避免设备仍然指向已移除的通道。
     */
    private void unbindSessionIndexes(ChannelSession session) {
        String deviceNo = session.getDeviceNo();
        if (StringUtils.isNotBlank(deviceNo)) {
            deviceNoToChannelId.remove(deviceNo, channelId(session));
        }
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
        ChannelSession session = findSessionByChannelId(channelId);
        if (session == null) {
            log.debug("通道 {} 不存在", channelId);
            return;
        }

        executeSyncInEventLoop(session, () -> {
            removeSessionInLoop(session);
            return null;
        });
    }

    /**
     * 在 EventLoop 中移除会话索引，并将队列中未完成的任务全部失败结束。
     */
    private ChannelSession removeSessionInLoop(ChannelSession session) {
        assertInEventLoop(session, "移除通道");
        String channelId = session.getChannel().id().asLongText();
        ChannelSession removedSession = sessions.remove(channelId);
        if (removedSession == null) {
            log.info("通道 {} ，绑定已被移除", channelId);
            return null;
        }
        unbindSessionIndexes(removedSession);

        // 处理掉未发送和挂起的任务
        IllegalStateException cause = new IllegalStateException("通道已移除");
        failQueuedTasks(removedSession, cause);
        finishPendingFailureInLoop(removedSession, cause, null);
        log.info("已移除通道 {} 的绑定", channelId);
        return removedSession;
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
        ChannelSession session = findSessionByChannelId(channelId);
        if (session == null) {
            log.warn("异常统计失败，通道 {} 不存在，原因 {}", channelId, reason);
            return false;
        }
        return executeSyncInEventLoop(session, () -> recordAbnormalInLoop(session, reason, nowMillis));
    }

    /**
     * 在窗口期内累计异常时间戳，并判断是否超过异常阈值。
     */
    private boolean recordAbnormalInLoop(ChannelSession session, AbnormalReasonEnum reason, long nowMillis) {
        assertInEventLoop(session, "记录异常频次");
        Deque<Long> timestamps = session.getAbnormalTimestamps();
        timestamps.addLast(nowMillis);
        while (!timestamps.isEmpty() && nowMillis - timestamps.peekFirst() > properties.getAbnormalWindowMillis()) {
            timestamps.removeFirst();
        }

        boolean exceeded = timestamps.size() > properties.getAbnormalMaxCount();
        if (exceeded) {
            log.warn("通道 {} 在 {}ms 内异常次数 {} 超过阈值 {}, 最新原因 {}",
                    channelId(session), properties.getAbnormalWindowMillis(), timestamps.size(),
                    properties.getAbnormalMaxCount(), reason);
        }
        return exceeded;
    }

    /**
     * 发送并等待设备响应（需要协议 handler 调用 completePending）
     *
     * @param deviceNo 设备No
     * @param message  消息
     * @return 响应数据
     * @throws IllegalArgumentException deviceNo为空
     */
    public CompletableFuture<byte[]> sendInQueue(String deviceNo, Object message) {
        ChannelSession session = requireActiveSessionByDeviceNo(deviceNo);

        PendingTask task = new PendingTask(deviceNo, message, new CompletableFuture<>(), true);
        return executeSyncInEventLoop(session, () -> enqueueTaskInLoop(session, task));
    }

    /**
     * 发送原始数据且不进入命令队列。
     * 该路径会切换到目标通道所属 EventLoop 异步执行。
     *
     * @param channelId 通道ID
     * @param payload   数据
     */
    public void sendDirectly(String channelId, byte[] payload) {
        ChannelSession session = findSessionByChannelId(channelId);
        if (session == null || payload == null) {
            return;
        }
        executeAsyncInEventLoop(session, () -> sendDirectlyInLoop(session, payload));
    }

    /**
     * 关闭通道并移除会话绑定；若会话已不存在则直接返回。
     */
    public void closeAndRemove(String channelId) {
        if (StringUtils.isBlank(channelId)) {
            return;
        }
        ChannelSession session = findSessionByChannelId(channelId);
        if (session == null) {
            log.debug("通道 {} 不存在，可能绑定已被移除", channelId);
            return;
        }

        executeSyncInEventLoop(session, () -> {
            closeSessionInLoop(session);
            return null;
        });
    }

    /**
     * 查询当前在线客户端快照列表。
     */
    public List<ChannelClientSnapshot> findClientSnapshotList() {
        List<ChannelClientSnapshot> snapshots = new ArrayList<>();
        for (ChannelSession session : new ArrayList<>(sessions.values())) {
            ChannelClientSnapshot snapshot = snapshotSession(session);
            if (snapshot != null) {
                snapshots.add(snapshot);
            }
        }
        return snapshots;
    }

    /**
     * 按设备编号查询当前在线客户端快照。
     *
     * @param deviceNo 设备编号
     * @return 运行态快照，不存在时返回 null
     */
    public ChannelClientSnapshot getClientSnapshotByDeviceNo(String deviceNo) {
        if (StringUtils.isBlank(deviceNo)) {
            return null;
        }
        return snapshotSession(findSessionByDeviceNo(deviceNo));
    }

    /**
     * 先移除会话状态，再关闭底层 Netty 通道。
     */
    private void closeSessionInLoop(ChannelSession session) {
        ChannelSession removedSession = removeSessionInLoop(session);
        if (isSessionActive(removedSession)) {
            removedSession.getChannel().close();
        }
    }

    /**
     * 在 EventLoop 中将任务压入发送队列；若通道不可用或队列已满则直接失败。
     */
    private CompletableFuture<byte[]> enqueueTaskInLoop(ChannelSession session, PendingTask task) {
        assertInEventLoop(session, "命令入队");
        if (!isSessionActive(session)) {
            task.future().completeExceptionally(new IllegalStateException("deviceNo " + task.deviceNo() + " 通道不活跃"));
            return task.future();
        }

        String channelId = channelId(session);
        Queue<PendingTask> queue = session.getQueue();
        if (queue.size() >= properties.getMaxQueueSize()) {
            task.future().completeExceptionally(new IllegalStateException("通道 " + channelId + " 队列已满"));
            return task.future();
        }
        queue.offer(task);
        dispatchNextInLoop(session);
        return task.future();
    }

    /**
     * 当当前没有发送中的命令时，从队列中取出下一条并启动发送。
     */
    private void dispatchNextInLoop(ChannelSession session) {
        assertInEventLoop(session, "派发下一条命令");
        Queue<PendingTask> queue = session.getQueue();
        // 未在发送时才取队列
        if (session.getSending().compareAndSet(false, true)) {
            PendingTask task = queue.poll();
            if (task == null) {
                session.getSending().set(false);
                return;
            }
            writeTaskInLoop(session, task);
        }
    }

    /**
     * 将任务真正写入通道；需要 ACK 的命令会登记 pending 并挂上超时处理。
     */
    private void writeTaskInLoop(ChannelSession session, PendingTask task) {
        assertInEventLoop(session, "发送命令");
        // 真正写出前再做一次最终可用性检查，兜住“入队后通道刚好失活”的竞态窗口。
        if (!isSessionActive(session)) {
            task.future().completeExceptionally(new IllegalStateException("通道不可用"));
            session.getSending().set(false);
            dispatchNextInLoop(session);
            return;
        }

        // 需要 ACK 的命令在写出前先登记为当前 pending，并注册超时任务；
        // 后续只有收到协议响应或发生写失败/超时，才会把该 pending 收口。
        if (task.requireAck()) {
            session.setPendingFuture(task.future());
            schedulePendingTimeoutInLoop(session, task);
        }

        session.getChannel().writeAndFlush(task.packet()).addListener((ChannelFutureListener) writeFuture -> {
            if (task.requireAck()) {
                // ACK 场景下，写成功仅表示“命令已发出”，真正完成要等协议响应；
                // 因此这里仅处理写失败，写成功时保持 sending/pending 状态不变。
                if (!writeFuture.isSuccess()) {
                    handlePendingWriteFailureInLoop(session, task, writeFuture.cause());
                }
                return;
            }

            // 非 ACK 场景下，写结果就是最终结果：写成功立即完成 future，
            // 写失败则直接失败，然后释放 sending 并继续派发下一条。
            if (!writeFuture.isSuccess()) {
                task.future().completeExceptionally(writeFuture.cause());
            } else {
                task.future().complete(new byte[0]);
            }
            session.getSending().set(false);
            dispatchNextInLoop(session);
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
        ChannelSession session = findSessionByDeviceNo(deviceNo);
        if (session == null) {
            log.debug("完成下行响应忽略：未找到会话，deviceNo={}", deviceNo);
            return false;
        }
        return executeSyncInEventLoop(session, () -> completePendingInLoop(session, payload));
    }

    /**
     * 按当前连接完成挂起命令，避免重连后旧 ACK 误落到新会话。
     */
    public boolean completePendingByChannelId(String channelId, byte[] payload) {
        if (StringUtils.isBlank(channelId)) {
            return false;
        }
        ChannelSession session = findSessionByChannelId(channelId);
        if (session == null) {
            log.debug("完成下行响应忽略：通道不存在，channelId={}", channelId);
            return false;
        }
        return executeSyncInEventLoop(session, () -> completePendingInLoop(session, payload));
    }

    /**
     * 在 EventLoop 中完成当前 pending 命令，并继续派发后续排队任务。
     */
    private boolean completePendingInLoop(ChannelSession session, byte[] payload) {
        assertInEventLoop(session, "完成挂起命令");
        boolean finished = finishPendingSuccessInLoop(session, payload);
        if (finished) {
            dispatchNextInLoop(session);
        }
        return finished;
    }

    /**
     * 为当前挂起命令注册超时任务，超时后关闭通道并清理队列。
     */
    private void schedulePendingTimeoutInLoop(ChannelSession session, PendingTask task) {
        assertInEventLoop(session, "注册命令超时任务");
        // @TODO 超时任务绑定在当前 session 自己的 EventLoop 上。
        // 一旦设备重连而旧 EventLoop 又恰好阻塞，旧 pending 的超时回调也会一起滞后；
        // 后续若要严格保证 commandTimeoutMillis，需要引入可跨重绑摘除的 retiring 状态
        // 或更独立的 timeout 管理机制。
        ScheduledFuture<?> timeoutFuture = session.getChannel().eventLoop().schedule(
                () -> handlePendingTimeoutInLoop(session, task.deviceNo(), task.future()),
                properties.getCommandTimeoutMillis(),
                TimeUnit.MILLISECONDS
        );
        session.setPendingTimeoutFuture(timeoutFuture);
    }

    /**
     * 处理 pending 命令超时：失败当前命令，并关闭当前通道与队列。
     */
    private void handlePendingTimeoutInLoop(ChannelSession session, String deviceNo, CompletableFuture<byte[]> expectedFuture) {
        assertInEventLoop(session, "处理命令超时");
        TimeoutException timeout = new TimeoutException("命令等待响应超时");
        boolean failed = finishPendingFailureInLoop(session, timeout, expectedFuture);
        if (!failed) {
            return;
        }
        String channelId = channelId(session);
        log.warn("下行命令超时，关闭通道并清理队列，deviceNo={} channelId={}", deviceNo, channelId);
        closeSessionInLoop(session);
    }

    /**
     * 处理写通道失败：失败当前命令，并继续尝试派发队列中的下一条任务。
     */
    private void handlePendingWriteFailureInLoop(ChannelSession session, PendingTask task, Throwable cause) {
        assertInEventLoop(session, "处理写失败");
        boolean failed = finishPendingFailureInLoop(session, cause, task.future());
        if (!failed) {
            return;
        }
        dispatchNextInLoop(session);
    }

    /**
     * 成功完成当前 pending 命令，并清空发送中状态。
     */
    private boolean finishPendingSuccessInLoop(ChannelSession session, byte[] payload) {
        CompletableFuture<byte[]> future = session.getPendingFuture();
        if (future == null) {
            log.debug("完成下行响应忽略：无挂起任务，deviceNo={} channelId={}",
                    session.getDeviceNo(), channelId(session));
            return false;
        }
        cancelPendingTimeout(session);
        session.setPendingFuture(null);
        future.complete(payload);
        session.getSending().set(false);
        return true;
    }

    /**
     * 失败完成当前 pending 命令；若 pending 已变化或不存在则返回 false。
     */
    private boolean finishPendingFailureInLoop(ChannelSession session, Throwable ex, CompletableFuture<byte[]> expectedFuture) {
        CompletableFuture<byte[]> currentFuture = session.getPendingFuture();
        if (currentFuture == null) {
            return false;
        }
        if (expectedFuture != null && currentFuture != expectedFuture) {
            return false;
        }
        cancelPendingTimeout(session);
        session.setPendingFuture(null);
        currentFuture.completeExceptionally(ex);
        session.getSending().set(false);
        return true;
    }

    /**
     * 按 channelId 查找当前会话；空白 channelId 直接返回 null。
     */
    private ChannelSession findSessionByChannelId(String channelId) {
        if (StringUtils.isBlank(channelId)) {
            return null;
        }
        return sessions.get(channelId);
    }

    /**
     * 按 deviceNo 查找会话；优先走缓存映射，命中脏数据时回退到全表扫描并修正映射。
     */
    private ChannelSession findSessionByDeviceNo(String deviceNo) {
        if (StringUtils.isBlank(deviceNo)) {
            return null;
        }

        ChannelSession mappedSession = findSessionFromMapping(deviceNo);
        if (mappedSession != null) {
            return mappedSession;
        }

        for (ChannelSession session : sessions.values()) {
            if (isMatchedDeviceSession(session, deviceNo)) {
                deviceNoToChannelId.put(deviceNo, channelId(session));
                return session;
            }
        }
        return null;
    }

    /**
     * 按 deviceNo 查找会话；未命中时抛出未找到通道异常。
     */
    private ChannelSession requireSessionByDeviceNo(String deviceNo) {
        ChannelSession session = findSessionByDeviceNo(deviceNo);
        if (session == null) {
            throw new IllegalStateException("deviceNo " + deviceNo + " 未找到通道");
        }
        return session;
    }

    /**
     * 按 deviceNo 查找当前可用会话；若通道已失活则抛出通道不活跃异常。
     */
    private ChannelSession requireActiveSessionByDeviceNo(String deviceNo) {
        if (StringUtils.isBlank(deviceNo)) {
            throw new IllegalArgumentException("deviceNo 不能为空");
        }

        ChannelSession session = requireSessionByDeviceNo(deviceNo);
        if (!isSessionActive(session)) {
            throw new IllegalStateException("deviceNo " + deviceNo + " 通道不活跃");
        }
        return session;
    }

    /**
     * 先按 deviceNo 索引命中会话；若索引已脏则顺手清理，交由调用方继续回扫。
     */
    private ChannelSession findSessionFromMapping(String deviceNo) {
        String channelId = deviceNoToChannelId.get(deviceNo);
        if (channelId == null) {
            return null;
        }

        ChannelSession session = findSessionByChannelId(channelId);
        if (isMatchedDeviceSession(session, deviceNo)) {
            return session;
        }

        deviceNoToChannelId.remove(deviceNo, channelId);
        return null;
    }

    /**
     * 判断当前会话是否与目标设备编号匹配，且底层通道仍然存在。
     */
    private boolean isMatchedDeviceSession(ChannelSession session, String deviceNo) {
        return session != null
                && session.getChannel() != null
                && deviceNo.equals(session.getDeviceNo());
    }

    /**
     * 将队列中尚未发送的任务全部按同一异常原因失败结束。
     */
    private void failQueuedTasks(ChannelSession session, Throwable cause) {
        Queue<PendingTask> queue = session.getQueue();
        PendingTask queuedTask;
        while ((queuedTask = queue.poll()) != null) {
            queuedTask.future().completeExceptionally(cause);
        }
    }

    /**
     * 在 EventLoop 中直接写出原始数据，不经过命令队列和 pending 管理。
     */
    private void sendDirectlyInLoop(ChannelSession session, byte[] payload) {
        assertInEventLoop(session, "直接发送数据");
        if (!isSessionActive(session)) {
            return;
        }
        session.getChannel().writeAndFlush(Unpooled.wrappedBuffer(payload));
        log.debug("通道 {} 直接发送数据 {}", channelId(session), HexUtil.bytesToHexString(payload));
    }

    /**
     * 构建单个会话的调试快照；当前会话已失效时返回 null。
     */
    private ChannelClientSnapshot snapshotSession(ChannelSession session) {
        if (session == null || session.getChannel() == null) {
            return null;
        }
        return toSnapshot(session);
    }

    /**
     * 调试快照采用尽力读取，避免逐个切换 EventLoop 放大查询延迟。
     */
    private ChannelClientSnapshot toSnapshot(ChannelSession session) {
        Channel channel = session.getChannel();
        return new ChannelClientSnapshot()
                .setChannelId(channelId(session))
                .setDeviceNo(session.getDeviceNo())
                .setDeviceType(session.getDeviceType())
                .setActive(channel.isActive())
                .setOpen(channel.isOpen())
                .setRegistered(channel.isRegistered())
                .setWritable(channel.isWritable())
                .setSending(session.getSending().get())
                .setPending(session.getPendingFuture() != null)
                .setQueueSize(session.getQueue().size())
                .setAbnormalCount(session.getAbnormalTimestamps().size())
                .setRemoteAddress(formatAddress(channel.remoteAddress()))
                .setLocalAddress(formatAddress(channel.localAddress()));
    }

    /**
     * 将 SocketAddress 转成便于展示的字符串地址。
     */
    private String formatAddress(SocketAddress address) {
        if (address == null) {
            return null;
        }
        if (address instanceof InetSocketAddress inetSocketAddress) {
            return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
        }
        return address.toString();
    }

    /**
     * 异步将任务投递到目标 EventLoop；若当前已在目标线程中则直接执行。
     */
    private void executeAsyncInEventLoop(ChannelSession session, Runnable task) {
        if (session == null || session.getChannel() == null) {
            throw new IllegalStateException("通道会话不存在");
        }
        EventLoop eventLoop = session.getChannel().eventLoop();
        if (eventLoop.inEventLoop()) {
            task.run();
            return;
        }

        eventLoop.execute(task);
    }

    /**
     * 在指定会话所属的 EventLoop 中执行并返回结果。
     * <p>
     * 语义：
     * 1) 若当前线程已在 EventLoop 中，则直接执行，避免额外调度开销；
     * 2) 若当前线程不在 EventLoop 中，则投递到 EventLoop，并等待执行结果返回。
     * <p>
     * 注意：该方法阻塞当前线程，等待 EventLoop 执行完成。
     * 不要从另一个 Netty IO 线程调用此方法，否则会阻塞那个 IO 线程。
     *
     * @param session  会话（用于定位 channel/eventLoop）
     * @param supplier 待执行逻辑
     * @param <T>      返回值类型
     * @return 执行结果
     */
    private <T> T executeSyncInEventLoop(ChannelSession session, Supplier<T> supplier) {
        if (session == null || session.getChannel() == null) {
            throw new IllegalStateException("通道会话不存在");
        }
        EventLoop eventLoop = session.getChannel().eventLoop();
        // 如果本身就在netty线程，那么直接处执行
        if (eventLoop.inEventLoop()) {
            return supplier.get();
        }
        if (Thread.currentThread() instanceof FastThreadLocalThread) {
            log.warn("executeSyncInEventLoop 被其他 Netty IO 线程调用，可能阻塞事件循环，targetChannelId={}",
                    channelId(session));
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
            return resultFuture.get(properties.getEventLoopWaitTimeoutMillis(), TimeUnit.MILLISECONDS);
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

    /**
     * 取消当前挂起命令的超时任务。
     */
    private void cancelPendingTimeout(ChannelSession session) {
        ScheduledFuture<?> timeoutFuture = session.getPendingTimeoutFuture();
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            session.setPendingTimeoutFuture(null);
        }
    }

    /**
     * 断言当前线程就在目标会话所属的 EventLoop 中执行。
     */
    private void assertInEventLoop(ChannelSession session, String action) {
        if (session == null || session.getChannel() == null) {
            throw new IllegalStateException("通道会话不存在");
        }
        if (!session.getChannel().eventLoop().inEventLoop()) {
            throw new IllegalStateException(action + " 必须在 EventLoop 中执行");
        }
    }

    /**
     * 判断会话底层通道当前是否仍然可用。
     */
    private boolean isSessionActive(ChannelSession session) {
        return session != null && session.getChannel() != null && session.getChannel().isActive();
    }

    /**
     * 提取会话对应的 channelId；会话或通道为空时返回 null。
     */
    private String channelId(ChannelSession session) {
        if (session == null || session.getChannel() == null) {
            return null;
        }
        return session.getChannel().id().asLongText();
    }
}
