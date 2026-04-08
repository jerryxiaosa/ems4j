package info.zhihui.ems.business.report.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("energy_report_daily_account")
public class DailyAccountReportEntity extends BaseEntity {

    private Integer id;

    private LocalDate reportDate;

    private Integer accountId;

    private Integer ownerId;

    private Integer ownerType;

    private String ownerName;

    private Integer electricAccountType;

    private Integer meterCount;

    private BigDecimal consumePower;

    private BigDecimal consumePowerHigher;

    private BigDecimal consumePowerHigh;

    private BigDecimal consumePowerLow;

    private BigDecimal consumePowerLower;

    private BigDecimal consumePowerDeepLow;

    private BigDecimal electricChargeAmount;

    private BigDecimal electricChargeAmountHigher;

    private BigDecimal electricChargeAmountHigh;

    private BigDecimal electricChargeAmountLow;

    private BigDecimal electricChargeAmountLower;

    private BigDecimal electricChargeAmountDeepLow;

    private BigDecimal monthlyChargeAmount;

    private BigDecimal correctionPayAmount;

    private BigDecimal correctionRefundAmount;

    private BigDecimal correctionNetAmount;

    private BigDecimal rechargeAmount;

    private BigDecimal rechargeServiceFeeAmount;

    private BigDecimal totalDebitAmount;

    private BigDecimal beginBalance;

    private BigDecimal endBalance;

    private BigDecimal accumulateConsumePower = BigDecimal.ZERO;

    private BigDecimal accumulateElectricChargeAmount = BigDecimal.ZERO;

    private BigDecimal accumulateMonthlyChargeAmount = BigDecimal.ZERO;

    private BigDecimal accumulateCorrectionPayAmount = BigDecimal.ZERO;

    private BigDecimal accumulateCorrectionRefundAmount = BigDecimal.ZERO;

    private BigDecimal accumulateRechargeAmount = BigDecimal.ZERO;

    private BigDecimal accumulateRechargeServiceFeeAmount = BigDecimal.ZERO;

    private BigDecimal accumulateTotalDebitAmount = BigDecimal.ZERO;
}
