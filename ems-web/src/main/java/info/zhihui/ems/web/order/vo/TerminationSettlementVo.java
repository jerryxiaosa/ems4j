package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销户订单结算信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "TerminationSettlementVo", description = "销户结算信息")
public class TerminationSettlementVo {

    @NotBlank
    @Schema(description = "销户编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cancelNo;

    @NotNull
    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer accountId;

    @NotNull
    @Schema(description = "账户归属者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerId;

    @NotBlank
    @Schema(description = "账户归属者类型，参考 ownerType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerType;

    @NotBlank
    @Schema(description = "账户归属者名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @NotNull
    @Schema(description = "结算金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal settlementAmount;

    @NotBlank
    @Schema(description = "电费计费类型标识，参考 electricAccountType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String electricAccountType;

    @NotNull
    @Schema(description = "涉及电表数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer electricMeterAmount;

    @NotNull
    @Schema(description = "是否全部销户", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean fullCancel;

    @NotEmpty
    @Schema(description = "电表ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> meterIdList;

    @Schema(description = "终止原因")
    private String closeReason;
}
