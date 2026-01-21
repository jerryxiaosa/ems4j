package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricTimeStartDto {

    private Integer type;
    private Integer hour;
    private Integer min;

}
