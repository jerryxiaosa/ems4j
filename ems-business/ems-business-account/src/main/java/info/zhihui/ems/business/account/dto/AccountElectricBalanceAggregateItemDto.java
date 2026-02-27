package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 账户电费余额聚合输入项
 */
@Data
@Accessors(chain = true)
public class AccountElectricBalanceAggregateItemDto {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 账户计费类型（允许为空，空值按未知类型处理，聚合结果返回0）
     */
    private ElectricAccountTypeEnum electricAccountType;
}
