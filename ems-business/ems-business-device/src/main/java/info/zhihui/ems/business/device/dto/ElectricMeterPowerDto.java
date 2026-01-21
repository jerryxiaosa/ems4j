package info.zhihui.ems.business.device.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterPowerDto {
    private Integer meterId;

    private BigDecimal power;
    private BigDecimal powerHigher;
    private BigDecimal powerHigh;
    private BigDecimal powerLow;
    private BigDecimal powerLower;
    private BigDecimal powerDeepLow;

    /**
     * 抄表时间
     */
    private LocalDateTime recordTime;
}
