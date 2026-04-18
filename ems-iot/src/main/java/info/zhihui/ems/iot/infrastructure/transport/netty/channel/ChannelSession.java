package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
@ToString(exclude = {"queue", "abnormalTimestamps", "pendingFuture", "pendingTimeoutFuture"})
@Accessors(chain = true)
public class ChannelSession {

    /** 设备编号。 */
    private volatile String deviceNo;
    /** 设备类型。 */
    private volatile DeviceTypeEnum deviceType;
    /** Netty 通道实例。 */
    private volatile Channel channel;
    /** 当前等待应答的命令 Future（同一时刻最多一个）。 */
    private volatile CompletableFuture<byte[]> pendingFuture;
    /** 当前挂起命令的超时任务句柄。 */
    private volatile ScheduledFuture<?> pendingTimeoutFuture;

    /** 待发送命令队列。 */
    private final Queue<PendingTask> queue = new ConcurrentLinkedQueue<>();
    /** 是否处于发送中状态。 */
    private final AtomicBoolean sending = new AtomicBoolean(false);
    /** 异常事件时间戳窗口，用于异常频次统计。 */
    private final Deque<Long> abnormalTimestamps = new ConcurrentLinkedDeque<>();
}
