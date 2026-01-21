package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 电表销户明细
 */
@Data
@Schema(name = "MeterCancelDetailVo", description = "电表销户明细")
public class MeterCancelDetailVo {

    @NotNull
    @Schema(description = "电表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer meterId;

    @Schema(description = "尖电量")
    private BigDecimal powerHigher;

    @Schema(description = "峰电量")
    private BigDecimal powerHigh;

    @Schema(description = "平电量")
    private BigDecimal powerLow;

    @Schema(description = "谷电量")
    private BigDecimal powerLower;

    @Schema(description = "深谷电量")
    private BigDecimal powerDeepLow;
}
