package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.config.DeviceAdapterProperties;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelAttributes;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyFrameDecoderProvider;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyProtocolDetector;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ProtocolFrameDecoderTest {

    @Test
    void decode_whenDetected_shouldInstallDecoderAndForward() {
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor("ACREL")
                .setTransportType(TransportProtocolEnum.TCP);
        NettyProtocolDetector detector = payload -> signature;
        NettyFrameDecoderProvider provider = new StubProvider(signature);
        ProtocolFrameDecoder decoder = new ProtocolFrameDecoder(List.of(detector), List.of(provider),
                new DeviceAdapterProperties.UnknownProtocolProperties());
        EmbeddedChannel channel = new EmbeddedChannel(decoder);
        byte[] payload = new byte[]{0x01, 0x02, 0x03};

        channel.writeInbound(Unpooled.wrappedBuffer(payload));

        Assertions.assertNotNull(channel.attr(ChannelAttributes.PROTOCOL_SIGNATURE).get());
        Assertions.assertNull(channel.pipeline().get(ProtocolFrameDecoder.class));
        Assertions.assertNotNull(channel.pipeline().get(StubHandler.class));

        ByteBuf inbound = channel.readInbound();
        Assertions.assertNotNull(inbound);
        byte[] actual = new byte[inbound.readableBytes()];
        inbound.readBytes(actual);
        ReferenceCountUtil.release(inbound);
        Assertions.assertArrayEquals(payload, actual);
    }

    @Test
    void decode_whenNoProvider_shouldCloseChannel() {
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor("ACREL")
                .setTransportType(TransportProtocolEnum.TCP);
        NettyProtocolDetector detector = payload -> signature;
        NettyFrameDecoderProvider provider = new NettyFrameDecoderProvider() {
            @Override
            public boolean supports(ProtocolSignature sig) {
                return false;
            }

            @Override
            public List<ChannelHandler> createDecoders(ProtocolSignature sig) {
                return List.of();
            }
        };
        ProtocolFrameDecoder decoder = new ProtocolFrameDecoder(List.of(detector), List.of(provider),
                new DeviceAdapterProperties.UnknownProtocolProperties());
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{0x01}));

        Assertions.assertFalse(channel.isActive());
    }

    @Test
    void decode_whenUndetected_shouldKeepDecoder() {
        NettyProtocolDetector detector = payload -> null;
        ProtocolFrameDecoder decoder = new ProtocolFrameDecoder(List.of(detector), List.of(),
                new DeviceAdapterProperties.UnknownProtocolProperties());
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{0x01, 0x02}));

        Assertions.assertNull(channel.attr(ChannelAttributes.PROTOCOL_SIGNATURE).get());
        Assertions.assertNotNull(channel.pipeline().get(ProtocolFrameDecoder.class));
        Assertions.assertNull(channel.readInbound());
    }

    private static class StubProvider implements NettyFrameDecoderProvider {

        private final ProtocolSignature signature;

        private StubProvider(ProtocolSignature signature) {
            this.signature = signature;
        }

        @Override
        public boolean supports(ProtocolSignature sig) {
            return signature.getVendor().equalsIgnoreCase(sig.getVendor());
        }

        @Override
        public List<ChannelHandler> createDecoders(ProtocolSignature sig) {
            return List.of(new StubHandler());
        }
    }

    private static class StubHandler extends ChannelInboundHandlerAdapter {
    }
}
