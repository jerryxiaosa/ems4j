package info.zhihui.ems.web.device.vo;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 电表开关状态设置请求
 */
@Data
@Schema(name = "ElectricMeterSwitchStatusVo", description = "电表开关状态设置参数")
public class ElectricMeterSwitchStatusVo {

    @NotNull
    @Schema(description = "电表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @NotNull
    @Schema(description = "开关状态编码，参考 electricSwitchStatus", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer switchStatus;

}
