package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDateTime;

@Slf4j
final class Acrel4gParseSupport {

    static final int SERIAL_NUMBER_LENGTH = 20;
    static final int TIME_SYNC_BODY_LENGTH = 7;

    private Acrel4gParseSupport() {
    }

    /**
     * 读取定长字符串，遇到 0x00 结束并去除首尾空白。
     */
    static String readString(byte[] data, int offset, int maxLen) {
        if (data == null || data.length <= offset) {
            return "";
        }
        int end = Math.min(data.length, offset + maxLen);
        int len = 0;
        for (int i = offset; i < end; i++) {
            if (data[i] == 0x00) {
                break;
            }
            len++;
        }
        return new String(data, offset, len, StandardCharsets.UTF_8).trim();
    }

    /**
     * 解析时间字段，支持带/不带星期。
     */
    static LocalDateTime parseDateTime(byte[] payload, int offset, boolean withDayOfWeek) {
        int length = withDayOfWeek ? TIME_SYNC_BODY_LENGTH : TIME_SYNC_BODY_LENGTH - 1;
        if (payload == null || payload.length < offset + length) {
            return null;
        }
        int idx = offset;
        int year = Byte.toUnsignedInt(payload[idx++]);
        int month = Byte.toUnsignedInt(payload[idx++]);
        int day = Byte.toUnsignedInt(payload[idx++]);
        Integer dayOfWeek = null;
        if (withDayOfWeek) {
            dayOfWeek = Byte.toUnsignedInt(payload[idx++]); // 周日=0
        }
        int hour = Byte.toUnsignedInt(payload[idx++]);
        int minute = Byte.toUnsignedInt(payload[idx++]);
        int second = Byte.toUnsignedInt(payload[idx]);
        int fullYear = 2000 + year;
        try {
            LocalDateTime time = LocalDateTime.of(fullYear, month, day, hour, minute, second);
            if (dayOfWeek != null) {
                int expectedDayOfWeek = time.getDayOfWeek().getValue() % 7;
                if (dayOfWeek != expectedDayOfWeek) {
                    log.warn("对时报文星期与日期不一致：dayOfWeek={} expected={} date={}", dayOfWeek, expectedDayOfWeek, time.toLocalDate());
                }
            }
            return time;
        } catch (DateTimeException ex) {
            if (dayOfWeek != null) {
                log.warn("对时报文字段非法：year={} month={} day={} dayOfWeek={} hour={} minute={} second={}",
                        fullYear, month, day, dayOfWeek, hour, minute, second);
            } else {
                log.warn("数据上报时间字段非法：year={} month={} day={} hour={} minute={} second={}",
                        fullYear, month, day, hour, minute, second);
            }
            return null;
        }
    }

    /**
     * 读取无符号 32 位整型（高字节在前）。
     * 示例：`00 00 01 00` -> 256。
     */
    static int readUInt32(byte[] data, int offset) {
        if (data == null || data.length < offset + 4) {
            return 0;
        }
        return (Byte.toUnsignedInt(data[offset]) << 24)
                | (Byte.toUnsignedInt(data[offset + 1]) << 16)
                | (Byte.toUnsignedInt(data[offset + 2]) << 8)
                | Byte.toUnsignedInt(data[offset + 3]);
    }
}
