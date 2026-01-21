package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ElectricDeviceDegreeDto extends BaseElectricDeviceDto {

    private ElectricPricePeriodEnum type;
}
