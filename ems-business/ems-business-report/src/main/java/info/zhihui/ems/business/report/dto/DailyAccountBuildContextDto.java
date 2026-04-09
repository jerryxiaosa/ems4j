package info.zhihui.ems.business.report.dto;

import info.zhihui.ems.business.billing.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.OrderFlowEntity;
import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 单日账户日报构建上下文。
 */
@Data
@Accessors(chain = true)
public class DailyAccountBuildContextDto {

    /**
     * 当前构建日的时间范围。
     */
    @Valid
    @NotNull
    private ReportDateRangeQo reportDateRange;

    /**
     * 前一日账户日报列表，用于余额和累计值承接。
     */
    private List<DailyAccountReportEntity> previousReportList;

    /**
     * 当前构建日已生成的电表日报列表，作为账户日报的直接输入。
     */
    private List<DailyMeterReportEntity> dailyMeterReportList;

    /**
     * 当前构建日的包月扣费流水。
     */
    private List<AccountBalanceConsumeRecordEntity> monthlyConsumeRecordList;

    /**
     * 当前构建日的账户余额流水。
     */
    private List<OrderFlowEntity> accountOrderFlowList;

    /**
     * 当前构建日的账户级补正记录。
     * 仅合并账户使用，按量账户补正仍通过电表日报汇总。
     */
    private List<ElectricMeterBalanceConsumeRecordEntity> accountCorrectionRecordList;

    /**
     * 当前构建日的充值到账记录。
     */
    private List<RechargeSourceItemQo> accountRechargeList;

    /**
     * 当前构建日的充值服务费记录。
     */
    private List<RechargeSourceItemQo> rechargeServiceFeeList;

    /**
     * 账户开户快照记录。
     */
    private List<AccountOpenRecordEntity> accountOpenRecordList;
}
