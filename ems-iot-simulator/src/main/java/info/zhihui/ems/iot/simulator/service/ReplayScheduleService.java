package info.zhihui.ems.iot.simulator.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 历史补投时间点生成服务。
 */
@Service
public class ReplayScheduleService {

    private final Clock clock;

    public ReplayScheduleService() {
        this(Clock.systemDefaultZone());
    }

    public ReplayScheduleService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    /**
     * 根据补投起止时间生成完整的历史上报点列表。
     */
    public List<LocalDateTime> buildReplayPoints(LocalDateTime startTime, LocalDateTime endTime) {
        return buildReplayPoints(startTime, endTime, null);
    }

    /**
     * 根据补投起止时间和已完成游标，生成后续仍需发送的历史上报点列表。
     */
    public List<LocalDateTime> buildReplayPoints(LocalDateTime startTime, LocalDateTime endTime,
                                                 LocalDateTime replayCursorTime) {
        validateHistoryRange(startTime, endTime);
        List<LocalDateTime> replayPoints = new ArrayList<>();
        LocalDateTime currentPointTime = resolveStartPoint(startTime, replayCursorTime);
        while (!currentPointTime.isAfter(endTime)) {
            replayPoints.add(currentPointTime);
            currentPointTime = currentPointTime.plusHours(1);
        }
        return replayPoints;
    }

    /**
     * 校验补投时间段合法，确保结束时间严格早于当前时间。
     */
    private void validateHistoryRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("补投时间范围不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("补投开始时间不能晚于结束时间");
        }
        if (!endTime.isBefore(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("补投结束时间必须是历史时间");
        }
    }

    /**
     * 根据上次成功补投的游标，决定本次补投的起始时间点。
     */
    private LocalDateTime resolveStartPoint(LocalDateTime startTime, LocalDateTime replayCursorTime) {
        if (replayCursorTime == null || replayCursorTime.isBefore(startTime)) {
            return startTime;
        }
        return replayCursorTime.plusHours(1);
    }
}
