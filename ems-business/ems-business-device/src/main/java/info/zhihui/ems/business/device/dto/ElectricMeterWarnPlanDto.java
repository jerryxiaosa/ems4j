package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterWarnPlanDto {

    /**
     * 预警计划ID
     */
    @NotNull(message = "预警计划ID不能为空")
    private Integer warnPlanId;

    /**
     * 电表ID列表
     */
    @NotEmpty(message = "电表ID列表不能为空")
    private List<Integer> meterIds;
}
