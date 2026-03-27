package info.zhihui.ems.iot.simulator.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 单个上报周期的电量增量。
 */
@Data
@Accessors(chain = true)
public class EnergyIncrement {

    private BigDecimal totalEnergyIncrement;
    private BigDecimal higherEnergyIncrement;
    private BigDecimal highEnergyIncrement;
    private BigDecimal lowEnergyIncrement;
    private BigDecimal lowerEnergyIncrement;
    private BigDecimal deepLowEnergyIncrement;
}
