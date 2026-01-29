package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.config.DeviceAdapterProperties;
import info.zhihui.ems.iot.plugins.acrel.protocol.constant.AcrelProtocolConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.transport.netty.decoder.Acrel4gFrameDecoderProvider;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.transport.netty.decoder.AcrelDelimitedFrameDecoder;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.transport.netty.decoder.AcrelGatewayFrameDecoder;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.transport.netty.decoder.AcrelGatewayFrameDecoderProvider;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ProtocolFrameDecoderPipelineTest {

    @Test
    void testDecode_WhenGatewaySignature_ShouldInstallGatewayFrameDecoderAndOutputFrame() {
        ProtocolFrameDecoder decoder = new ProtocolFrameDecoder(
                List.of(new AcrelGatewayFrameDecoderProvider(), new Acrel4gFrameDecoderProvider()),
                new DeviceAdapterProperties.UnknownProtocolProperties());
        EmbeddedChannel channel = new EmbeddedChannel(decoder);
        byte[] frame = new byte[]{
                0x1f, 0x1f, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x7f
        };

        channel.writeInbound(Unpooled.wrappedBuffer(frame));

        Assertions.assertNull(channel.pipeline().get(ProtocolFrameDecoder.class));
        Assertions.assertNotNull(channel.pipeline().get(AcrelGatewayFrameDecoder.class));
        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }

    @Test
    void testDecode_WhenDirectSignature_ShouldInstallDelimitedFrameDecoderAndOutputFrame() {
        ProtocolFrameDecoder decoder = new ProtocolFrameDecoder(
                List.of(new AcrelGatewayFrameDecoderProvider(), new Acrel4gFrameDecoderProvider()),
                new DeviceAdapterProperties.UnknownProtocolProperties());
        EmbeddedChannel channel = new EmbeddedChannel(decoder);
        byte[] frame = new byte[]{
                AcrelProtocolConstants.DELIMITER, AcrelProtocolConstants.DELIMITER,
                0x01, 0x02, 0x03, 0x7d, 0x7d
        };

        channel.writeInbound(Unpooled.wrappedBuffer(frame));

        Assertions.assertNull(channel.pipeline().get(ProtocolFrameDecoder.class));
        Assertions.assertNotNull(channel.pipeline().get(AcrelDelimitedFrameDecoder.class));
        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }
}
