package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterSwitchStatusDto {
    @NotNull(message = "电表ID不能为空")
    private Integer id;

    @NotNull(message = "电表开关状态不能为空")
    private ElectricSwitchStatusEnum switchStatus;

    @NotNull(message = "命令来源不能为空")
    private CommandSourceEnum commandSource;
}
