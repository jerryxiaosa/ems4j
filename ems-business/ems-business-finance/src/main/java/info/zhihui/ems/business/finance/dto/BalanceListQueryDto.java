package info.zhihui.ems.business.finance.dto;

import info.zhihui.ems.common.enums.BalanceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 余额明细列表查询条件
 */
@Data
@Accessors(chain = true)
public class BalanceListQueryDto {

    /**
     * 账户ID列表
     */
    private List<Integer> accountIds;

    /**
     * 余额关联ID列表（如账户ID、电表ID）
     */
    private List<Integer> balanceRelationIds;

    /**
     * 余额类型
     */
    private BalanceTypeEnum balanceType;
}
