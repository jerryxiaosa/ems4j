package info.zhihui.ems.business.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 账户信息更新请求
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
    @DecimalMin(value = "1", message = "月租费必须大于或等于1")
    private BigDecimal monthlyPayAmount;

    /**
     * 联系人
     */
    @Size(max = 50, message = "联系人长度不能超过50")
    private String contactName;

    /**
     * 联系方式
     */
    @Size(max = 40, message = "联系方式长度不能超过40")
    private String contactPhone;
}
