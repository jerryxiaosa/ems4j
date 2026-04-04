package info.zhihui.ems.iot.simulator.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

class ReplayScheduleServiceTest {

    private final ReplayScheduleService replayScheduleService = new ReplayScheduleService(fixedClock());

    @Test
    void buildReplayPoints_whenHistoryRangeValid_shouldUseStartTimeAsAnchor() {
        List<LocalDateTime> replayPoints = replayScheduleService.buildReplayPoints(
                LocalDateTime.of(2026, 2, 1, 17, 0, 8),
                LocalDateTime.of(2026, 2, 1, 20, 30, 0));

        Assertions.assertEquals(List.of(
                LocalDateTime.of(2026, 2, 1, 17, 0, 8),
                LocalDateTime.of(2026, 2, 1, 18, 0, 8),
                LocalDateTime.of(2026, 2, 1, 19, 0, 8),
                LocalDateTime.of(2026, 2, 1, 20, 0, 8)
        ), replayPoints);
    }

    @Test
    void buildReplayPoints_whenEndTimeIsNotHistory_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> replayScheduleService.buildReplayPoints(
                LocalDateTime.of(2026, 3, 24, 10, 23, 15),
                LocalDateTime.of(2026, 3, 24, 10, 23, 15)));
    }

    @Test
    void buildReplayPoints_whenStartTimeIsAfterEndTime_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> replayScheduleService.buildReplayPoints(
                LocalDateTime.of(2026, 2, 2, 17, 0, 8),
                LocalDateTime.of(2026, 2, 1, 17, 0, 8)));
    }

    @Test
    void buildReplayPoints_whenRangeIsMissing_shouldDefaultToCurrentMonth() {
        List<LocalDateTime> replayPoints = replayScheduleService.buildReplayPoints(null, null);

        Assertions.assertFalse(replayPoints.isEmpty());
        Assertions.assertEquals(LocalDateTime.of(2026, 3, 1, 0, 0, 0), replayPoints.get(0));
        Assertions.assertEquals(LocalDateTime.of(2026, 3, 24, 10, 0, 0), replayPoints.get(replayPoints.size() - 1));
        Assertions.assertEquals(563, replayPoints.size());
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-03-24T10:23:15Z"), ZoneId.of("UTC"));
    }
}
