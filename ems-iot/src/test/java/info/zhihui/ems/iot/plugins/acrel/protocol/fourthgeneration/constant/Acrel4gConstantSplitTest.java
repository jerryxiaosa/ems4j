package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Acrel4gConstantSplitTest {

    @Test
    void shouldExposeFrameLevelConstants() {
        Assertions.assertEquals((byte) 0x7b, Acrel4gFrameConstants.DELIMITER);
        Assertions.assertEquals((byte) 0x7d, Acrel4gFrameConstants.DELIMITER_END);
    }

    @Test
    void shouldExposePayloadLevelConstants() {
        Assertions.assertEquals(20, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH);
        Assertions.assertEquals(58, Acrel4gPayloadConstants.REGISTER_BODY_LENGTH);
        Assertions.assertEquals(7, Acrel4gPayloadConstants.TIME_SYNC_BODY_LENGTH);
        Assertions.assertEquals(0x60, Acrel4gPayloadConstants.DATA_LENGTH_RATE4);
        Assertions.assertEquals(0x90, Acrel4gPayloadConstants.DATA_LENGTH_RATE8);
        Assertions.assertEquals((byte) 0x5b, Acrel4gPayloadConstants.SECTION_START);
        Assertions.assertEquals((byte) 0x29, Acrel4gPayloadConstants.MODBUS_END);
    }
}
