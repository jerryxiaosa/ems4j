package info.zhihui.ems.business.plan.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class StepDto {

    private BigDecimal start;

    private BigDecimal end;
}
