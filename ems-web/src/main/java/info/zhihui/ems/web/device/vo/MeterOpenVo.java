package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 电表开户请求
 */
@Data
@Schema(name = "MeterOpenVo", description = "电表开户参数")
public class MeterOpenVo {

    @Valid
    @NotEmpty
    @Size(max = 100)
    @Schema(description = "电表开户明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MeterOpenDetailVo> meterOpenDetail;

    @NotNull
    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer accountId;

    @NotNull
    @Schema(description = "账户归属者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerId;

    @NotNull
    @Schema(description = "账户归属者类型编码，参考 ownerType", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerType;

    @NotEmpty
    @Schema(description = "账户归属者名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @NotNull
    @Schema(description = "电费账户类型编码，参考 electricAccountType", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer electricAccountType;

    @Schema(description = "电费计费方案ID")
    private Integer electricPricePlanId;

    @Schema(description = "预警计划ID")
    private Integer warnPlanId;
}
