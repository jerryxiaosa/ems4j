package info.zhihui.ems.business.billing.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电表区间耗电趋势点
 */
@Data
@Accessors(chain = true)
public class ElectricMeterPowerConsumeTrendPointDto {

    /**
     * 开始记录时间
     */
    private LocalDateTime beginRecordTime;

    /**
     * 结束记录时间
     */
    private LocalDateTime endRecordTime;

    /**
     * 电表消费时间
     */
    private LocalDateTime meterConsumeTime;

    /**
     * 总消费电量
     */
    private BigDecimal consumePower;

    /**
     * 尖消费电量
     */
    private BigDecimal consumePowerHigher;

    /**
     * 峰消费电量
     */
    private BigDecimal consumePowerHigh;

    /**
     * 平消费电量
     */
    private BigDecimal consumePowerLow;

    /**
     * 谷消费电量
     */
    private BigDecimal consumePowerLower;

    /**
     * 深谷消费电量
     */
    private BigDecimal consumePowerDeepLow;
}
