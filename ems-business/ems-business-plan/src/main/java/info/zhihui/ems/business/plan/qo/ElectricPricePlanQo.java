package info.zhihui.ems.business.plan.qo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricPricePlanQo {

    private String name;

    private Boolean isCustomPrice;

    private Integer neId;

    private String eqName;

}
