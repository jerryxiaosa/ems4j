package info.zhihui.ems.business.billing.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 余额明细列表查询对象
 */
@Data
@Accessors(chain = true)
public class BalanceListQueryQo {

    private List<Integer> accountIds;

    private List<Integer> balanceRelationIds;

    private Integer balanceType;
}
