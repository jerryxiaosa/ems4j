package info.zhihui.ems.business.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 账户配置更新请求
 */
@Data
@Accessors(chain = true)
public class AccountConfigUpdateDto {
    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 电价方案ID
     */
    private Integer electricPricePlanId;

    /**
     * 预警方案ID
     */
    private Integer warnPlanId;

    /**
     * 月租费用（仅包月账户）
     */
    @DecimalMin(value = "1", message = "月租费必须大于1")
    private BigDecimal monthlyPayAmount;
}
