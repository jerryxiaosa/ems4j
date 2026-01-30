package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.transport.netty.decoder;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import io.netty.channel.ChannelHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AcrelGatewayFrameDecoderProviderTest {

    @Test
    void detectTcp_whenHeadMatch_shouldReturnSignature() {
        AcrelGatewayFrameDecoderProvider provider = new AcrelGatewayFrameDecoderProvider();
        byte[] payload = new byte[]{0x1f, 0x1f};

        ProtocolSignature signature = provider.detectTcp(payload);

        Assertions.assertNotNull(signature);
        Assertions.assertEquals(VendorEnum.ACREL.name(), signature.getVendor());
        Assertions.assertEquals(DeviceAccessModeEnum.GATEWAY, signature.getAccessMode());
        Assertions.assertEquals(TransportProtocolEnum.TCP, signature.getTransportType());
    }

    @Test
    void detectTcp_whenHeadMismatch_shouldReturnNull() {
        AcrelGatewayFrameDecoderProvider provider = new AcrelGatewayFrameDecoderProvider();
        byte[] payload = new byte[]{0x00, 0x01};

        Assertions.assertNull(provider.detectTcp(payload));
    }

    @Test
    void createDecoders_shouldReturnGatewayDecoder() {
        AcrelGatewayFrameDecoderProvider provider = new AcrelGatewayFrameDecoderProvider();
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor(VendorEnum.ACREL.name())
                .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                .setTransportType(TransportProtocolEnum.TCP);

        List<ChannelHandler> handlers = provider.createDecoders(signature);

        Assertions.assertEquals(1, handlers.size());
        Assertions.assertInstanceOf(AcrelGatewayFrameDecoder.class, handlers.get(0));
    }
}
