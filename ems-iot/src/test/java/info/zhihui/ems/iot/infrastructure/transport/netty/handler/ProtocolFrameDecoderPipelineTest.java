package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.config.DeviceAdapterProperties;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyProtocolDetector;
import info.zhihui.ems.iot.plugins.acrel.constants.AcrelProtocolConstants;
import info.zhihui.ems.iot.plugins.acrel.transport.tcp.frame.AcrelDelimitedFrameDecoder;
import info.zhihui.ems.iot.plugins.acrel.transport.tcp.frame.AcrelGatewayFrameDecoder;
import info.zhihui.ems.iot.plugins.acrel.transport.tcp.frame.AcrelTcpFrameDecoderProvider;
import info.zhihui.ems.iot.protocol.port.ProtocolSignature;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ProtocolFrameDecoderPipelineTest {

    @Test
    void testDecode_WhenGatewaySignature_ShouldInstallGatewayFrameDecoderAndOutputFrame() {
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor(AcrelProtocolConstants.VENDOR)
                .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                .setTransportType(TransportProtocolEnum.TCP);
        NettyProtocolDetector detector = payload -> signature;
        ProtocolFrameDecoder decoder = new ProtocolFrameDecoder(List.of(detector),
                List.of(new AcrelTcpFrameDecoderProvider()),
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
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor(AcrelProtocolConstants.VENDOR)
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setTransportType(TransportProtocolEnum.TCP);
        NettyProtocolDetector detector = payload -> signature;
        ProtocolFrameDecoder decoder = new ProtocolFrameDecoder(List.of(detector),
                List.of(new AcrelTcpFrameDecoderProvider()),
                new DeviceAdapterProperties.UnknownProtocolProperties());
        EmbeddedChannel channel = new EmbeddedChannel(decoder);
        byte[] frame = new byte[]{
                0x7b, 0x7b, 0x01, 0x02, 0x03, 0x7d, 0x7d
        };

        channel.writeInbound(Unpooled.wrappedBuffer(frame));

        Assertions.assertNull(channel.pipeline().get(ProtocolFrameDecoder.class));
        Assertions.assertNotNull(channel.pipeline().get(AcrelDelimitedFrameDecoder.class));
        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }
}
