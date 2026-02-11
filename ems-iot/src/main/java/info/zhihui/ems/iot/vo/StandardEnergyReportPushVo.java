package info.zhihui.ems.iot.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 标准电量上报推送参数。
 */
@Data
@Accessors(chain = true)
public class StandardEnergyReportPushVo {

    private String source;
    private String sourceReportId;
    private String deviceNo;
    private LocalDateTime recordTime;
    private BigDecimal totalEnergy;
    private BigDecimal higherEnergy;
    private BigDecimal highEnergy;
    private BigDecimal lowEnergy;
    private BigDecimal lowerEnergy;
    private BigDecimal deepLowEnergy;
}
