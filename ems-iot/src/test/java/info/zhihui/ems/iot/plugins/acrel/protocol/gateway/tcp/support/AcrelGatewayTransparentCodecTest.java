package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayTransparentMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGatewayTransparentCodecTest {

    @Test
    void testEncode_WhenMeterIdNull_ShouldReturnNull() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        Assertions.assertNull(codec.encode(null, new byte[]{0x01}));
    }

    @Test
    void testEncode_WhenPayloadNull_ShouldReturnNullPayloadText() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        Assertions.assertEquals("m1|null", codec.encode("m1", null));
    }

    @Test
    void testDecode_WhenTextBlank_ShouldReturnNull() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        Assertions.assertNull(codec.decode(" "));
    }

    @Test
    void testDecode_WhenSeparatorMissing_ShouldReturnNull() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        Assertions.assertNull(codec.decode("meter-1"));
    }

    @Test
    void testDecode_WhenSeparatorAtEnd_ShouldReturnNull() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        Assertions.assertNull(codec.decode("meter-1|"));
    }

    @Test
    void testDecode_WhenTextValid_ShouldReturnMessage() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        GatewayTransparentMessage message = codec.decode("m1|0A0B");

        Assertions.assertNotNull(message);
        Assertions.assertEquals("m1", message.meterId());
        Assertions.assertArrayEquals(new byte[]{0x0a, 0x0b}, message.payload());
    }
}
