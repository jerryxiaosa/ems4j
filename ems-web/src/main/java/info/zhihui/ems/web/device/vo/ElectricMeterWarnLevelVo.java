package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 电表预警等级设置
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricMeterWarnLevelVo", description = "电表预警等级设置参数")
public class ElectricMeterWarnLevelVo {

    @NotEmpty
    @Schema(description = "电表ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> meterIds;

    @NotBlank
    @Schema(description = "预警类型标识，参考 warnType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warnType;
}
