package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 电表保电模式设置
 */
@Data
@Schema(name = "ElectricMeterProtectVo", description = "电表保电模式设置参数")
public class ElectricMeterProtectVo {

    @NotEmpty
    @Schema(description = "电表ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> meterIds;

    @NotNull
    @Schema(description = "是否启用保电模式", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean protect;
}
