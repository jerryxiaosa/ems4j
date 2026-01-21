package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ElectricPriceTimeUpdateDto extends BaseElectricDeviceDto {

    private Integer plan;

    private List<ElectricTimeStartDto> electricDurations;
}
