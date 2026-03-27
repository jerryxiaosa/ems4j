package info.zhihui.ems.iot.simulator.config;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史补投配置。
 */
@Data
public class SimulatorReplayProperties {

    /**
     * 是否启用历史补投。
     */
    private Boolean enabled = false;

    /**
     * 历史补投开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 历史补投结束时间。
     */
    private LocalDateTime endTime;

    /**
     * 补投发送间隔，毫秒。
     */
    private int sendIntervalMs = 200;
}
