package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import java.util.concurrent.CompletableFuture;

public record PendingTask(String deviceNo, Object packet, CompletableFuture<byte[]> future, boolean requireAck) {
}
