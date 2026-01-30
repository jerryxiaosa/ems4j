package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.transport.netty.decoder;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.constant.AcrelProtocolConstants;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import io.netty.channel.ChannelHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class Acrel4gFrameDecoderProviderTest {

    @Test
    void detectTcp_whenDelimiterMatch_shouldReturnSignature() {
        Acrel4gFrameDecoderProvider provider = new Acrel4gFrameDecoderProvider();
        byte[] payload = new byte[]{AcrelProtocolConstants.DELIMITER, AcrelProtocolConstants.DELIMITER};

        ProtocolSignature signature = provider.detectTcp(payload);

        Assertions.assertNotNull(signature);
        Assertions.assertEquals(VendorEnum.ACREL.name(), signature.getVendor());
        Assertions.assertEquals(DeviceAccessModeEnum.DIRECT, signature.getAccessMode());
        Assertions.assertEquals(TransportProtocolEnum.TCP, signature.getTransportType());
    }

    @Test
    void detectTcp_whenDelimiterMismatch_shouldReturnNull() {
        Acrel4gFrameDecoderProvider provider = new Acrel4gFrameDecoderProvider();
        byte[] payload = new byte[]{0x00, 0x01};

        Assertions.assertNull(provider.detectTcp(payload));
    }

    @Test
    void createDecoders_shouldReturnDelimitedDecoder() {
        Acrel4gFrameDecoderProvider provider = new Acrel4gFrameDecoderProvider();
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor(VendorEnum.ACREL.name())
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setTransportType(TransportProtocolEnum.TCP);

        List<ChannelHandler> handlers = provider.createDecoders(signature);

        Assertions.assertEquals(1, handlers.size());
        Assertions.assertInstanceOf(AcrelDelimitedFrameDecoder.class, handlers.get(0));
    }
}
