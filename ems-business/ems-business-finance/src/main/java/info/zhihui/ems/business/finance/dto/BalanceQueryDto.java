package info.zhihui.ems.business.finance.dto;

import info.zhihui.ems.common.enums.BalanceTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BalanceQueryDto {
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


}
