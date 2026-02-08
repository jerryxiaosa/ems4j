package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DailyEnergyPlanQueryDto extends BaseElectricDeviceDto {

    private Integer dailyPlanId;
}
