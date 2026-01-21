package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预警方案查询
 */
@Data
@Schema(name = "WarnPlanQueryVo", description = "预警方案查询条件")
public class WarnPlanQueryVo {

    @Schema(description = "方案名称")
    private String name;
}
