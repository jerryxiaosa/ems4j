package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 电表在线状态同步
 */
@Data
@Schema(name = "ElectricMeterOnlineStatusVo", description = "电表在线状态同步参数")
public class ElectricMeterOnlineStatusVo {

    @NotNull
    @Schema(description = "电表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer meterId;

    @Schema(description = "是否在线")
    private Boolean onlineStatus;

    @Schema(description = "是否强制同步")
    private Boolean force;
}
