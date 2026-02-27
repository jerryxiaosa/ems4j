package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 开户请求 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "OpenAccountVo", description = "账户开户请求参数")
public class OpenAccountVo {

    @NotNull
    @Schema(description = "账户归属者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerId;

    @NotNull
    @Schema(description = "账户类型编码，参考 ownerType", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerType;

    @NotBlank
    @Schema(description = "账户归属者名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @Size(max = 50, message = "联系人长度不能超过50")
    @Schema(description = "联系人")
    private String contactName;

    @Size(max = 40, message = "联系方式长度不能超过40")
    @Schema(description = "联系方式")
    private String contactPhone;

    @NotNull
    @Schema(description = "电费计费类型编码，参考 electricAccountType", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer electricAccountType;

    @Schema(description = "月度缴费金额，包月必填")
    @DecimalMin(value = "1", message = "月度缴费金额需大于或等于1")
    private BigDecimal monthlyPayAmount;

    @Schema(description = "电价方案ID，非包月必填")
    private Integer electricPricePlanId;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;

    @Schema(description = "是否继承历史阶梯量，默认 false")
    private Boolean inheritHistoryPower;

    @Valid
    @NotEmpty
    @Schema(description = "开户表列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MeterOpenDetailVo> electricMeterList;

}
