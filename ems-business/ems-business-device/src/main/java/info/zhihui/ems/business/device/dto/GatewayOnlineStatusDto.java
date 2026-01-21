package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 网关在线状态同步 DTO
 */
@Data
@Accessors(chain = true)
public class GatewayOnlineStatusDto {
    @NotNull(message = "网关ID不能为空")
    private Integer gatewayId;

    private Boolean onlineStatus;

    private Boolean force;
}
