package info.zhihui.ems.iot.plugins.acrel.command.support;

import java.util.ArrayList;
import java.util.List;

/**
 * 三字节一组的时段解析器（type、minute、hour）。
 */
public final class AcrelTripleSlotParser {

    private AcrelTripleSlotParser() {
    }

    public static List<TripleSlot> parse(byte[] data, String lengthError, String timeError) {
        if (data == null || data.length == 0) {
            return List.of();
        }
        if (data.length % 3 != 0) {
            throw new IllegalArgumentException(lengthError);
        }
        int end = data.length;
        while (end >= 3
                && data[end - 1] == 0
                && data[end - 2] == 0
                && data[end - 3] == 0) {
            end -= 3;
        }
        if (end == 0) {
            return List.of();
        }
        List<TripleSlot> slots = new ArrayList<>(end / 3);
        for (int i = 0; i < end; i += 3) {
            int type = Byte.toUnsignedInt(data[i]);
            int minute = Byte.toUnsignedInt(data[i + 1]);
            int hour = Byte.toUnsignedInt(data[i + 2]);
            if (hour > 23 || minute > 59) {
                throw new IllegalArgumentException(timeError);
            }
            slots.add(new TripleSlot(type, minute, hour));
        }
        return slots;
    }

    public record TripleSlot(int type, int minute, int hour) {
    }
}
