package info.zhihui.ems.web.device.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电表最近一次上报电量记录
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricMeterLatestPowerRecordVo", description = "电表最近一次上报电量记录")
public class ElectricMeterLatestPowerRecordVo {

    @Schema(description = "抄表时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    @Schema(description = "总读数")
    private BigDecimal power;

    @Schema(description = "尖读数")
    private BigDecimal powerHigher;

    @Schema(description = "峰读数")
    private BigDecimal powerHigh;

    @Schema(description = "平读数")
    private BigDecimal powerLow;

    @Schema(description = "谷读数")
    private BigDecimal powerLower;

    @Schema(description = "深谷读数")
    private BigDecimal powerDeepLow;
}
