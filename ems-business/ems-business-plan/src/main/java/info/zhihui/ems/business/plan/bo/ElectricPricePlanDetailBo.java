package info.zhihui.ems.business.plan.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ElectricPricePlanDetailBo extends ElectricPricePlanBo {

    private List<StepPriceBo> stepPrices;

}
