package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterTimeDto {
    @NotNull(message = "电表ID不能为空")
    private Integer id;

    @NotNull(message = "命令来源不能为空")
    private CommandSourceEnum commandSource;

    @NotNull(message = "电表时间段不能为空")
    private List<ElectricPriceTimeDto> timeList;
}
