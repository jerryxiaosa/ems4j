package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayTransparentMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class AcrelGatewayTransparentCodecTest {

    @Test
    void testEncode_WhenMeterIdNull_ShouldThrow() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> codec.encode(null, 1, new byte[]{0x01}));
    }

    @Test
    void testEncode_WhenPayloadNull_ShouldThrow() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> codec.encode(1, 2, null));
    }

    @Test
    void testEncode_WhenPayloadPresent_ShouldReturnBytes() {
        AcrelGatewayTransparentCodec codec = new AcrelGatewayTransparentCodec();

        byte[] encoded = codec.encode(1, 2, new byte[]{0x0A, 0x0B});

        Assertions.assertNotNull(encoded);
        Assertions.assertEquals("01002|0a0b", new String(encoded, StandardCharsets.UTF_8));
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
