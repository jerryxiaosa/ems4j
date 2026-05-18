package info.zhihui.ems.business.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 账户日报区间汇总业务对象。
 */
@Data
@Accessors(chain = true)
public class AccountDailyReportSummaryBo {

    private Integer accountId;

    private BigDecimal consumePower;

    private BigDecimal electricChargeAmount;

    /**
     * 按账户类型解析后的费用金额。
     * 包月账户取包月扣费，其余账户取按量电费。
     */
    private BigDecimal resolvedChargeAmount;
}
