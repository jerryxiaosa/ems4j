package info.zhihui.ems.iot.plugins.acrel.command.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AcrelTripleSlotParserTest {

    @Test
    void parse_whenDataNullOrEmpty_shouldReturnEmpty() {
        List<AcrelTripleSlotParser.TripleSlot> slots = AcrelTripleSlotParser.parse(
                null, "len", "time");
        Assertions.assertTrue(slots.isEmpty());

        slots = AcrelTripleSlotParser.parse(new byte[0], "len", "time");
        Assertions.assertTrue(slots.isEmpty());
    }

    @Test
    void parse_whenTrailingZeros_shouldTrim() {
        byte[] data = new byte[]{
                0x01, 0x02, 0x03,
                0x04, 0x05, 0x06,
                0x00, 0x00, 0x00
        };

        List<AcrelTripleSlotParser.TripleSlot> slots = AcrelTripleSlotParser.parse(
                data, "len", "time");

        Assertions.assertEquals(2, slots.size());
        Assertions.assertEquals(1, slots.get(0).type());
        Assertions.assertEquals(2, slots.get(0).minute());
        Assertions.assertEquals(3, slots.get(0).hour());
        Assertions.assertEquals(4, slots.get(1).type());
        Assertions.assertEquals(5, slots.get(1).minute());
        Assertions.assertEquals(6, slots.get(1).hour());
    }

    @Test
    void parse_whenLengthInvalid_shouldThrow() {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04};

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> AcrelTripleSlotParser.parse(data, "len-error", "time-error"));

        Assertions.assertEquals("len-error", ex.getMessage());
    }

    @Test
    void parse_whenTimeInvalid_shouldThrow() {
        byte[] data = new byte[]{0x01, 0x3C, 0x00};

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> AcrelTripleSlotParser.parse(data, "len-error", "time-error"));

        Assertions.assertEquals("time-error", ex.getMessage());
    }
}
