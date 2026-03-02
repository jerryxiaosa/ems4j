package info.zhihui.ems.web.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OfflineDurationUtilTest {

    @Test
    void testFormat_IsOnline_ReturnsNull() {
        String result = OfflineDurationUtil.format(true, LocalDateTime.now().minusHours(2));

        assertNull(result);
    }

    @Test
    void testFormat_LastOnlineTimeNull_ReturnsNull() {
        String result = OfflineDurationUtil.format(false, null);

        assertNull(result);
    }

    @Test
    void testFormat_LastOnlineTimeAfterCurrentTime_ReturnsNull() {
        String result = OfflineDurationUtil.format(false, LocalDateTime.now().plusMinutes(1));

        assertNull(result);
    }

    @Test
    void testFormat_LessThanOneMinute_ReturnsOneMinute() {
        String result = OfflineDurationUtil.format(false, LocalDateTime.now().minusSeconds(20));

        assertEquals("1分钟", result);
    }

    @Test
    void testFormat_LessThanOneDay_ReturnsHourText() {
        String result = OfflineDurationUtil.format(false, LocalDateTime.now().minusHours(2).minusMinutes(59));

        assertEquals("2小时", result);
    }

    @Test
    void testFormat_GreaterThanOrEqualOneDay_ReturnsDayText() {
        String result = OfflineDurationUtil.format(false, LocalDateTime.now().minusDays(1).minusHours(23).minusMinutes(59));

        assertEquals("1天", result);
    }
}
