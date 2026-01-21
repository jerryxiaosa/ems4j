package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 阶梯电价
 */
@Data
@Accessors(chain = true)
@Schema(name = "StepPriceVo", description = "阶梯电价配置")
public class StepPriceVo {

    @Schema(description = "起始用量")
    private BigDecimal start;

    @Schema(description = "结束用量")
    private BigDecimal end;

    @Schema(description = "倍率")
    private BigDecimal value;
}
