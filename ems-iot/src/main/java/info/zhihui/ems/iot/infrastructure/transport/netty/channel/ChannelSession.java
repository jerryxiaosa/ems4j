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

    private String deviceNo;
    private DeviceTypeEnum deviceType;
    private Channel channel;
    private CompletableFuture<byte[]> pendingFuture;

    private final Queue<PendingTask> queue = new ArrayDeque<>();
    private final AtomicBoolean sending = new AtomicBoolean(false);
    private final Deque<Long> abnormalTimestamps = new ArrayDeque<>();
}
