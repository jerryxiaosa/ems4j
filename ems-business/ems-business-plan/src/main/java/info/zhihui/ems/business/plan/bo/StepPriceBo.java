package info.zhihui.ems.business.plan.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class StepPriceBo {

    private BigDecimal start;

    private BigDecimal end;

    /**
     * 电价对应的是倍率
     */
    private BigDecimal value;
}

