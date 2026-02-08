package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import info.zhihui.ems.common.model.energy.DatePlanItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DateEnergyPlanUpdateDto extends BaseElectricDeviceDto {

    private List<DatePlanItem> items;
}
