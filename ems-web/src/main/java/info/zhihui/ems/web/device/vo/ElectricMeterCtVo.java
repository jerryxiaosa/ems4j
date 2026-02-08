package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

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
    @Positive
    @Max(65535)
    @Schema(description = "CT变比", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ct;
}
