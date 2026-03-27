package info.zhihui.ems.iot.simulator.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单次计算后的累计电量快照。
 */
@Data
@Accessors(chain = true)
public class EnergySnapshot {

    private Boolean shouldReport;
    private LocalDateTime reportTime;
    private BigDecimal totalEnergy;
    private BigDecimal higherEnergy;
    private BigDecimal highEnergy;
    private BigDecimal lowEnergy;
    private BigDecimal lowerEnergy;
    private BigDecimal deepLowEnergy;
    private EnergyIncrement increment;
}
