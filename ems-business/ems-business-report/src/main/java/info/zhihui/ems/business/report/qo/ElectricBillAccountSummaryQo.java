package info.zhihui.ems.business.report.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电费报表账户汇总查询结果。
 */
@Data
@Accessors(chain = true)
public class ElectricBillAccountSummaryQo {

    /**
     * 账户ID。
     */
    private Integer accountId;

    /**
     * 账户名称。
     */
    private String accountName;

    /**
     * 电价计费类型编码。
     */
    private Integer electricAccountType;

    /**
     * 本期电量汇总。
     */
    private BigDecimal periodConsumePower;

    /**
     * 本期按量电费汇总。
     */
    private BigDecimal periodElectricChargeAmount;

    /**
     * 本期包月费用汇总。
     */
    private BigDecimal periodMonthlyChargeAmount;

    /**
     * 本期充值金额汇总。
     */
    private BigDecimal periodRechargeAmount;

    /**
     * 本期补正净额汇总。
     */
    private BigDecimal periodCorrectionAmount;

    /**
     * 本期合计费用汇总。
     */
    private BigDecimal totalDebitAmount;
}
