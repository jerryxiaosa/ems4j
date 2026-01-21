package info.zhihui.ems.business.finance.dto;

import info.zhihui.ems.common.enums.BalanceTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class BalanceDto {
    /**
     * 账户关联id
     */
    @NotNull(message = "账户关联id不能为空")
    private Integer balanceRelationId;

    /**
     * 账户类型
     */
    @NotNull(message = "账户类型不能为空")
    private BalanceTypeEnum balanceType;

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 订单号
     */
    @NotNull(message = "订单号不能为空")
    private String orderNo;

    /**
     * 充值金额
     */
    @DecimalMin(value = "0.0", message = "充值金额必须大于0")
    private BigDecimal amount;

}
