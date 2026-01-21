package info.zhihui.ems.business.plan.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricPricePlanQueryDto {

    private String name;

    private Boolean isCustomPrice;

    private Integer neId;

    private String eqName;

}
