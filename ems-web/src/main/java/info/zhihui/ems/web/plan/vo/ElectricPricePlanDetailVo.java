package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 电价方案详细信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "ElectricPricePlanDetailVo", description = "电价方案详情")
public class ElectricPricePlanDetailVo extends ElectricPricePlanVo {

    @Schema(description = "阶梯电价配置")
    private List<StepPriceVo> stepPrices;
}
