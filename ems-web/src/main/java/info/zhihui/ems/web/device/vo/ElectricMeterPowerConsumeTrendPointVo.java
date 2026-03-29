package info.zhihui.ems.web.device.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电表区间耗电趋势点
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricMeterPowerConsumeTrendPointVo", description = "电表区间耗电趋势点")
public class ElectricMeterPowerConsumeTrendPointVo {

    @Schema(description = "开始记录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginRecordTime;

    @Schema(description = "结束记录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endRecordTime;

    @Schema(description = "电表消费时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime meterConsumeTime;

    @Schema(description = "总消费电量")
    private BigDecimal consumePower;

    @Schema(description = "尖消费电量")
    private BigDecimal consumePowerHigher;

    @Schema(description = "峰消费电量")
    private BigDecimal consumePowerHigh;

    @Schema(description = "平消费电量")
    private BigDecimal consumePowerLow;

    @Schema(description = "谷消费电量")
    private BigDecimal consumePowerLower;

    @Schema(description = "深谷消费电量")
    private BigDecimal consumePowerDeepLow;
}
