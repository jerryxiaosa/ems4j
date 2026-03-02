package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 能耗充值订单明细
 */
@Data
@Accessors(chain = true)
@Schema(name = "EnergyTopUpDetailVo", description = "能耗充值订单明细")
public class EnergyTopUpDetailVo {

    @NotNull
    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer accountId;

    @NotBlank
    @Schema(description = "账户余额类型标识，参考 balanceType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String balanceType;

    @NotBlank
    @Schema(description = "账户归属者类型，参考 ownerType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerType;

    @NotNull
    @Schema(description = "账户归属者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerId;

    @NotBlank
    @Schema(description = "账户归属者名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @NotBlank
    @Schema(description = "电费账户类型标识，参考 electricAccountType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String electricAccountType;

    @Schema(description = "电表ID")
    private Integer meterId;

    @Schema(description = "电表类型标识，参考 meterType")
    private String meterType;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "电表编号")
    private String deviceNo;

    @Schema(description = "空间ID")
    private Integer spaceId;
}
