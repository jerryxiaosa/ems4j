package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Accessors(chain = true)
public class ChannelSession {

    /** 设备编号。 */
    private String deviceNo;
    /** 设备类型。 */
    private DeviceTypeEnum deviceType;
    /** Netty 通道实例。 */
    private Channel channel;
    /** 当前等待应答的命令 Future（同一时刻最多一个）。 */
    private CompletableFuture<byte[]> pendingFuture;

    /** 待发送命令队列。 */
    private final Queue<PendingTask> queue = new ArrayDeque<>();
    /** 是否处于发送中状态。 */
    private final AtomicBoolean sending = new AtomicBoolean(false);
    /** 异常事件时间戳窗口，用于异常频次统计。 */
    private final Deque<Long> abnormalTimestamps = new ArrayDeque<>();
}
