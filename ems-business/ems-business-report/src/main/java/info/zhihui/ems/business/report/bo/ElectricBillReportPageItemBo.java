package info.zhihui.ems.business.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电费报表列表项。
 */
@Data
@Accessors(chain = true)
public class ElectricBillReportPageItemBo {

    private Integer accountId;

    private String accountName;

    private Integer electricAccountType;

    private Integer meterCount;

    private BigDecimal periodConsumePower;

    private BigDecimal periodElectricChargeAmount;

    private BigDecimal periodRechargeAmount;

    private BigDecimal periodCorrectionAmount;

    private BigDecimal totalDebitAmount;
}
