package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class Acrel4gParseSupportTest {

    @Test
    void readString_whenNullOrOffsetBeyond_shouldReturnEmpty() {
        Assertions.assertEquals("", Acrel4gParseSupport.readString(null, 0, 5));
        Assertions.assertEquals("", Acrel4gParseSupport.readString(new byte[]{0x01}, 2, 5));
    }

    @Test
    void readString_shouldStopAtNullAndTrim() {
        byte[] data = new byte[]{' ', 'A', 'B', 0x00, 'C'};
        Assertions.assertEquals("AB", Acrel4gParseSupport.readString(data, 0, 5));
    }

    @Test
    void parseDateTime_withDayOfWeek_shouldReturnTime() {
        byte[] payload = new byte[]{0x15, 0x0a, 0x15, 0x04, 0x13, 0x24, 0x2a};
        LocalDateTime time = Acrel4gParseSupport.parseDateTime(payload, 0, true);
        Assertions.assertEquals(LocalDateTime.of(2021, 10, 21, 19, 36, 42), time);
    }

    @Test
    void parseDateTime_withDayOfWeekMismatch_shouldStillReturnTime() {
        byte[] payload = new byte[]{0x15, 0x0a, 0x15, 0x00, 0x13, 0x24, 0x2a};
        LocalDateTime time = Acrel4gParseSupport.parseDateTime(payload, 0, true);
        Assertions.assertEquals(LocalDateTime.of(2021, 10, 21, 19, 36, 42), time);
    }

    @Test
    void parseDateTime_withoutDayOfWeek_shouldReturnTime() {
        byte[] payload = new byte[]{0x15, 0x0a, 0x15, 0x13, 0x24, 0x2a};
        LocalDateTime time = Acrel4gParseSupport.parseDateTime(payload, 0, false);
        Assertions.assertEquals(LocalDateTime.of(2021, 10, 21, 19, 36, 42), time);
    }

    @Test
    void parseDateTime_withDayOfWeekInvalid_shouldReturnNull() {
        byte[] payload = new byte[]{0x15, 0x0d, 0x15, 0x00, 0x13, 0x24, 0x2a};
        Assertions.assertNull(Acrel4gParseSupport.parseDateTime(payload, 0, true));
    }

    @Test
    void parseDateTime_withoutDayOfWeekInvalid_shouldReturnNull() {
        byte[] payload = new byte[]{0x15, 0x0d, 0x15, 0x13, 0x24, 0x2a};
        Assertions.assertNull(Acrel4gParseSupport.parseDateTime(payload, 0, false));
    }

    @Test
    void parseDateTime_whenLengthInsufficient_shouldReturnNull() {
        byte[] payload = new byte[]{0x15, 0x0a};
        Assertions.assertNull(Acrel4gParseSupport.parseDateTime(payload, 0, true));
        Assertions.assertNull(Acrel4gParseSupport.parseDateTime(payload, 0, false));
    }

    @Test
    void readUInt32_shouldReturnZeroWhenShort() {
        Assertions.assertEquals(0, Acrel4gParseSupport.readUInt32(null, 0));
        Assertions.assertEquals(0, Acrel4gParseSupport.readUInt32(new byte[]{0x01}, 0));
    }

    @Test
    void readUInt32_shouldReturnValue() {
        byte[] data = new byte[]{0x00, 0x00, 0x01, 0x00};
        Assertions.assertEquals(256, Acrel4gParseSupport.readUInt32(data, 0));
    }
}
