package info.zhihui.ems.web.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class CorrectionMeterAmountVo {

    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer accountId;

    @Schema(description = "电表ID（按需计费必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer meterId;

    @Schema(description = "补正类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer correctionType;

    @Schema(description = "补正金额（大于0）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal amount;

    @Schema(description = "补正原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String reason;
}
