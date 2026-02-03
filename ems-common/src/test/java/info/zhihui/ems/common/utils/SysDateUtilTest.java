package info.zhihui.ems.common.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SysDateUtilTest {

    @Test
    public void testToOffsetDateTime() {
        // 测试正常时间转换
        LocalDateTime normalDateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 0);
        String result = SysDateUtil.toOffsetDateTime(normalDateTime);
        assertEquals("2024-03-15T14:30:00.000+08:00", result);

        // 测试午夜时间
        LocalDateTime midnightDateTime = LocalDateTime.of(2024, 3, 15, 0, 0, 0);
        result = SysDateUtil.toOffsetDateTime(midnightDateTime);
        assertEquals("2024-03-15T00:00:00.000+08:00", result);

        // 测试23:59:59时间
        LocalDateTime endOfDayDateTime = LocalDateTime.of(2024, 3, 15, 23, 59, 59);
        result = SysDateUtil.toOffsetDateTime(endOfDayDateTime);
        assertEquals("2024-03-15T23:59:59.000+08:00", result);

        // 测试带毫秒的时间
        LocalDateTime dateTimeWithMillis = LocalDateTime.of(2024, 3, 15, 14, 30, 0, 123000000);
        result = SysDateUtil.toOffsetDateTime(dateTimeWithMillis);
        assertEquals("2024-03-15T14:30:00.123+08:00", result);

        // 测试月初时间
        LocalDateTime startOfMonthDateTime = LocalDateTime.of(2024, 3, 1, 0, 0, 0);
        result = SysDateUtil.toOffsetDateTime(startOfMonthDateTime);
        assertEquals("2024-03-01T00:00:00.000+08:00", result);

        // 测试月末时间
        LocalDateTime endOfMonthDateTime = LocalDateTime.of(2024, 3, 31, 23, 59, 59);
        result = SysDateUtil.toOffsetDateTime(endOfMonthDateTime);
        assertEquals("2024-03-31T23:59:59.000+08:00", result);

        // 测试年初时间
        LocalDateTime startOfYearDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        result = SysDateUtil.toOffsetDateTime(startOfYearDateTime);
        assertEquals("2024-01-01T00:00:00.000+08:00", result);

        // 测试年末时间
        LocalDateTime endOfYearDateTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        result = SysDateUtil.toOffsetDateTime(endOfYearDateTime);
        assertEquals("2024-12-31T23:59:59.000+08:00", result);

        // 测试空值处理
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SysDateUtil.toOffsetDateTime(null));
        assertEquals("localDateTime must not be null", exception.getMessage());
    }

    @Test
    public void testToDateTimeZoneString() {
        // 测试正常时间转换
        LocalDateTime normalDateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 0);
        String result = SysDateUtil.toDateTimeZoneString(normalDateTime);
        assertEquals("2024-03-15T14:30:00+08:00", result);

        // 测试午夜时间
        LocalDateTime midnightDateTime = LocalDateTime.of(2024, 3, 15, 0, 0, 0);
        result = SysDateUtil.toDateTimeZoneString(midnightDateTime);
        assertEquals("2024-03-15T00:00:00+08:00", result);

        // 测试23:59:59时间
        LocalDateTime endOfDayDateTime = LocalDateTime.of(2024, 3, 15, 23, 59, 59);
        result = SysDateUtil.toDateTimeZoneString(endOfDayDateTime);
        assertEquals("2024-03-15T23:59:59+08:00", result);

        // 测试月初时间
        LocalDateTime startOfMonthDateTime = LocalDateTime.of(2024, 3, 1, 0, 0, 0);
        result = SysDateUtil.toDateTimeZoneString(startOfMonthDateTime);
        assertEquals("2024-03-01T00:00:00+08:00", result);

        // 测试月末时间
        LocalDateTime endOfMonthDateTime = LocalDateTime.of(2024, 3, 31, 23, 59, 59);
        result = SysDateUtil.toDateTimeZoneString(endOfMonthDateTime);
        assertEquals("2024-03-31T23:59:59+08:00", result);

        // 测试年初时间
        LocalDateTime startOfYearDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        result = SysDateUtil.toDateTimeZoneString(startOfYearDateTime);
        assertEquals("2024-01-01T00:00:00+08:00", result);

        // 测试年末时间
        LocalDateTime endOfYearDateTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        result = SysDateUtil.toDateTimeZoneString(endOfYearDateTime);
        assertEquals("2024-12-31T23:59:59+08:00", result);

        // 测试空值处理
        result = SysDateUtil.toDateTimeZoneString(null);
        assertEquals("", result);
    }

    @Test
    public void testToYearMonth_NullOrBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SysDateUtil.toYearMonth(null));
        assertEquals("Invalid date format. Expected format: yyyy-MM", exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class,
                () -> SysDateUtil.toYearMonth(" "));
        assertEquals("Invalid date format. Expected format: yyyy-MM", exception.getMessage());
    }

    @Test
    public void testToYearMonth_InvalidLength() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SysDateUtil.toYearMonth("2024-8"));
        assertEquals("Invalid date format. Expected format: yyyy-MM", exception.getMessage());
    }

    @Test
    public void testToDateString_Blank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SysDateUtil.toDateString((String) null));
        assertEquals("dateTimeString must not be blank", exception.getMessage());
    }
}
