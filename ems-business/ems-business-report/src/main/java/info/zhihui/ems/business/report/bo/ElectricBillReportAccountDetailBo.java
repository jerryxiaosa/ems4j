package info.zhihui.ems.business.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电费报表账户详情。
 */
@Data
@Accessors(chain = true)
public class ElectricBillReportAccountDetailBo {

    /**
     * 账户ID。
     */
    private Integer accountId;

    /**
     * 账户名称。
     */
    private String accountName;

    /**
     * 联系人。
     */
    private String contactName;

    /**
     * 联系方式。
     */
    private String contactPhone;

    /**
     * 电价计费类型编码。
     */
    private Integer electricAccountType;

    /**
     * 包月费用。
     */
    private BigDecimal monthlyPayAmount;

    /**
     * 统计结束日账户余额。
     */
    private BigDecimal accountBalance;

    /**
     * 区间内参与统计的去重电表数量。
     */
    private Integer meterCount;

    /**
     * 本期电量。
     */
    private BigDecimal periodConsumePower;

    /**
     * 本期电费。
     */
    private BigDecimal periodElectricChargeAmount;

    /**
     * 本期充值。
     */
    private BigDecimal periodRechargeAmount;

    /**
     * 本期补正。
     */
    private BigDecimal periodCorrectionAmount;

    /**
     * 统计日期文本。
     */
    private String dateRangeText;
}
