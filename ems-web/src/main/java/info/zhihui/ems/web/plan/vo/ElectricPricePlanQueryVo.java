package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 电价方案查询条件
 */
@Data
@Schema(name = "ElectricPricePlanQueryVo", description = "电价方案查询条件")
public class ElectricPricePlanQueryVo {

    @Schema(description = "方案名称")
    private String name;

    @Schema(description = "是否自定义价格")
    private Boolean isCustomPrice;

    @Schema(description = "排除的方案ID")
    private Integer neId;

    @Schema(description = "名称完全匹配")
    private String eqName;
}
