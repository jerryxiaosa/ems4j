package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 预警方案保存参数
 */
@Data
@Accessors(chain = true)
@Schema(name = "WarnPlanSaveVo", description = "预警方案保存参数")
public class WarnPlanSaveVo {

    @Schema(description = "方案ID，编辑时必填")
    private Integer id;

    @NotBlank
    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "第一阶段预警余额")
    private BigDecimal firstLevel;

    @Schema(description = "第二阶段预警余额")
    private BigDecimal secondLevel;

    @Schema(description = "欠费是否自动断闸")
    private Boolean autoClose;

    @Schema(description = "备注")
    private String remark;
}
