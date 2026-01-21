package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ElectricDeviceCTDto extends BaseElectricDeviceDto {

    private BigDecimal ct;
}
