package info.zhihui.ems.business.plan.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ElectricPricePlanQo {

    private List<Integer> ids;

    private String name;

    private Boolean isCustomPrice;

    private Integer neId;

    private String eqName;

}
