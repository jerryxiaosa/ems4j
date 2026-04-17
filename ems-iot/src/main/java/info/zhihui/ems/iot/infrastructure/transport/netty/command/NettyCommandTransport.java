package info.zhihui.ems.iot.infrastructure.transport.netty.command;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
    public boolean completePending(ProtocolSession session, byte[] payload) {
        if (session == null || StringUtils.isBlank(session.getSessionId())) {
            return false;
        }
        return channelManager.completePendingByChannelId(session.getSessionId(), payload);
    }
}
