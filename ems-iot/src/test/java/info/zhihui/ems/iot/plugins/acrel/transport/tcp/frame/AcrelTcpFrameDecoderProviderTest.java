package info.zhihui.ems.iot.plugins.acrel.transport.tcp.frame;

import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.plugins.acrel.constants.AcrelProtocolConstants;
import io.netty.channel.ChannelHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AcrelTcpFrameDecoderProviderTest {

    @Test
    void supports_whenVendorMatch_shouldReturnTrue() {
        AcrelTcpFrameDecoderProvider provider = new AcrelTcpFrameDecoderProvider();
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor(AcrelProtocolConstants.VENDOR)
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setTransportType(TransportProtocolEnum.TCP);

        Assertions.assertTrue(provider.supports(signature));
    }

    @Test
    void supports_whenVendorMismatch_shouldReturnFalse() {
        AcrelTcpFrameDecoderProvider provider = new AcrelTcpFrameDecoderProvider();
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor("OTHER")
                .setTransportType(TransportProtocolEnum.TCP);

        Assertions.assertFalse(provider.supports(signature));
    }

    @Test
    void createDecoders_whenGateway_shouldReturnGatewayDecoder() {
        AcrelTcpFrameDecoderProvider provider = new AcrelTcpFrameDecoderProvider();
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor(AcrelProtocolConstants.VENDOR)
                .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                .setTransportType(TransportProtocolEnum.TCP);

        List<ChannelHandler> handlers = provider.createDecoders(signature);

        Assertions.assertEquals(1, handlers.size());
        Assertions.assertInstanceOf(AcrelGatewayFrameDecoder.class, handlers.get(0));
    }

    @Test
    void createDecoders_whenDirect_shouldReturnDelimitedDecoder() {
        AcrelTcpFrameDecoderProvider provider = new AcrelTcpFrameDecoderProvider();
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor(AcrelProtocolConstants.VENDOR)
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setTransportType(TransportProtocolEnum.TCP);

        List<ChannelHandler> handlers = provider.createDecoders(signature);

        Assertions.assertEquals(1, handlers.size());
        Assertions.assertInstanceOf(AcrelDelimitedFrameDecoder.class, handlers.get(0));
    }
}
