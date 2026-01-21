package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GatewayPacketCodeTest {

    @Test
    void commandKey_whenPositiveByte_shouldReturnLowerHex() {
        Assertions.assertEquals("01", GatewayPacketCode.commandKey((byte) 0x01));
    }

    @Test
    void commandKey_whenNegativeByte_shouldReturnUnsignedHex() {
        Assertions.assertEquals("f2", GatewayPacketCode.commandKey((byte) 0xF2));
    }
}
