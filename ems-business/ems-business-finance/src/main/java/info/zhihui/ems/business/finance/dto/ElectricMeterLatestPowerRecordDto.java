package info.zhihui.ems.business.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电表最近一次上报电量记录
 */
@Data
@Accessors(chain = true)
public class ElectricMeterLatestPowerRecordDto {

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
