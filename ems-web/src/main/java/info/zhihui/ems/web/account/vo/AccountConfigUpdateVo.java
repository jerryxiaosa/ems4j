package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 账户信息更新 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountConfigUpdateVo", description = "账户信息更新参数")
public class AccountConfigUpdateVo {

    @Schema(description = "电价方案ID")
    private Integer electricPricePlanId;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;

    @DecimalMin(value = "1", message = "月租费必须大于或等于1")
    @Schema(description = "月度缴费金额，仅包月账户可设置")
    private BigDecimal monthlyPayAmount;

    @Size(max = 50, message = "联系人长度不能超过50")
    @Schema(description = "联系人（与联系方式需同时传）")
    private String contactName;

    @Size(max = 40, message = "联系方式长度不能超过40")
    @Schema(description = "联系方式（与联系人需同时传）")
    private String contactPhone;
}
