package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 销户订单创建请求
 */
@Data
@Accessors(chain = true)
@Schema(name = "TerminationOrderCreateVo", description = "销户订单创建参数")
public class TerminationOrderCreateVo {

    @NotNull
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userId;

    @NotBlank
    @Schema(description = "用户手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userPhone;

    @NotBlank
    @Schema(description = "用户真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userRealName;

    @NotBlank
    @Schema(description = "第三方用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String thirdPartyUserId;

    @NotNull
    @Schema(description = "订单金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal orderAmount;

    @Valid
    @NotNull
    @Schema(description = "销户结算信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private TerminationSettlementVo terminationInfo;
}
