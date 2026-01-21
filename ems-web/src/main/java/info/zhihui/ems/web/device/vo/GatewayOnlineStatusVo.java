package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 网关在线状态同步参数
 */
@Data
@Schema(name = "GatewayOnlineStatusVo", description = "网关在线状态同步参数")
public class GatewayOnlineStatusVo {

    @NotNull
    @Schema(description = "网关ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gatewayId;

    @Schema(description = "是否在线")
    private Boolean onlineStatus;

    @Schema(description = "是否强制同步")
    private Boolean force;
}
