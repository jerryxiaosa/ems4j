package info.zhihui.ems.business.billing.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电表用电趋势点
 */
@Data
@Accessors(chain = true)
public class ElectricMeterPowerTrendPointDto {

    /**
     * 抄表时间
     */
    private LocalDateTime recordTime;

    /**
     * 总读数
     */
    private BigDecimal power;

    /**
     * 尖读数
     */
    private BigDecimal powerHigher;

    /**
     * 峰读数
     */
    private BigDecimal powerHigh;

    /**
     * 平读数
     */
    private BigDecimal powerLow;

    /**
     * 谷读数
     */
    private BigDecimal powerLower;

    /**
     * 深谷读数
     */
    private BigDecimal powerDeepLow;
}
