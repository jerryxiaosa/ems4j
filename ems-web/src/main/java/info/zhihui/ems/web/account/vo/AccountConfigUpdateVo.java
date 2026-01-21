package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 账户配置更新 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountConfigUpdateVo", description = "账户配置更新参数")
public class AccountConfigUpdateVo {

    @Schema(description = "电价方案ID")
    private Integer electricPricePlanId;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;

    @DecimalMin(value = "1", message = "月度缴费金额需大于或等于1")
    @Schema(description = "月度缴费金额，仅包月账户可设置")
    private BigDecimal monthlyPayAmount;
}
