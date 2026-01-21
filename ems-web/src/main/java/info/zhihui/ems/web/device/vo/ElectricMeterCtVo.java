package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 电表CT变比设置
 */
@Data
@Schema(name = "ElectricMeterCtVo", description = "电表CT变比设置参数")
public class ElectricMeterCtVo {

    @NotNull
    @Schema(description = "电表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer meterId;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Schema(description = "CT变比", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal ct;
}
