package info.zhihui.ems.business.finance.bo;

import info.zhihui.ems.common.enums.BalanceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class BalanceBo {
    /**
     * 账户id
     */
    private Integer id;

    /**
     * 账户关联id
     */
    private Integer balanceRelationId;

    /**
     * 余额类型
     * @see BalanceTypeEnum
     */
    private BalanceTypeEnum balanceType;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 余额金额
     */
    private BigDecimal balance;

}
