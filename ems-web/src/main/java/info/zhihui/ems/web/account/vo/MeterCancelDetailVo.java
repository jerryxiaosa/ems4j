package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 销户表计明细 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "MeterCancelDetailVo", description = "销户表计明细")
public class MeterCancelDetailVo {

    @NotNull
    @Schema(description = "表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer meterId;

    @Schema(description = "高峰尖电量")
    private BigDecimal powerHigher;

    @Schema(description = "高峰电量")
    private BigDecimal powerHigh;

    @Schema(description = "平段电量")
    private BigDecimal powerLow;

    @Schema(description = "低谷电量")
    private BigDecimal powerLower;

    @Schema(description = "深谷电量")
    private BigDecimal powerDeepLow;
}
