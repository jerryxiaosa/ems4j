package info.zhihui.ems.iot.infrastructure.transport.netty.command;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.protocol.port.ProtocolCommandTransport;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 基于 Netty 的命令传输适配器。
 */
@Component
@RequiredArgsConstructor
public class NettyCommandTransport implements ProtocolCommandTransport {

    private final ChannelManager channelManager;

    @Override
    public CompletableFuture<byte[]> sendWithAck(String deviceNo, byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload 不能为空");
        }
        return channelManager.sendInQueue(deviceNo, Unpooled.wrappedBuffer(payload));
    }

    @Override
    public boolean completePending(String deviceNo, byte[] payload) {
        return channelManager.completePending(deviceNo, payload);
    }

    @Override
    public void failPending(String deviceNo, Throwable ex) {
        channelManager.failPending(deviceNo, ex);
    }
}
