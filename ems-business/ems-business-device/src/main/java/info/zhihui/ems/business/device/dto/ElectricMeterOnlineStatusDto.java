package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterOnlineStatusDto {
    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

    private Boolean onlineStatus;

    private Boolean force;
}
