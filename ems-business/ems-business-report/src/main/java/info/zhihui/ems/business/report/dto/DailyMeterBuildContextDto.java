package info.zhihui.ems.business.report.dto;

import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.entity.OpenMeterEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.qo.DailyMeterCandidateQo;
import info.zhihui.ems.business.report.qo.MeterSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.PowerRecordSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DailyMeterBuildContextDto {

    /**
     * 当前构建日的时间范围。
     */
    @Valid
    @NotNull
    private ReportDateRangeQo reportDateRange;

    /**
     * 当天需要生成日电表报表的候选账户-电表列表。
     */
    private List<DailyMeterCandidateQo> candidateList;

    /**
     * 前一日电表日报列表，用于零日报承接和类型兜底。
     */
    private List<DailyMeterReportEntity> previousReportList;

    /**
     * 当前构建日的用电区间记录。
     * 用于计算日初/日末读数、总用电量和分时用电量。
     */
    private List<ElectricMeterPowerConsumeRecordEntity> powerConsumeRecordList;

    /**
     * 当前构建日的按量扣费记录。
     * 用于计算按量电费、分时电费和展示单价。
     */
    private List<ElectricMeterBalanceConsumeRecordEntity> electricChargeRecordList;

    /**
     * 当前构建日的补正记录。
     * 用于计算补缴、退费和净补正金额。
     */
    private List<ElectricMeterBalanceConsumeRecordEntity> correctionRecordList;

    /**
     * 当前构建日的电表充值到账记录。
     * 仅在电表钱包场景下参与日报充值金额统计。
     */
    private List<RechargeSourceItemQo> meterRechargeList;

    /**
     * 当前构建日的电表开户记录，用于生命周期和首日读数承接。
     */
    private List<OpenMeterEntity> openRecordList;

    /**
     * 当前构建日的电表销户记录，用于生命周期和销户快照承接。
     */
    private List<MeterCancelRecordEntity> cancelRecordList;

    /**
     * 当前构建日的电表上报快照。
     * 来源于 powerRecord 与 powerRelation 的关联结果，
     * 优先用于补齐 meter/device/space 等当日展示快照。
     */
    private List<PowerRecordSnapshotSourceQo> powerRecordSnapshotList;

    /**
     * 账户开户快照记录。
     * 这里提供稳定的 electricAccountType、ownerId、ownerType、ownerName，
     * 不再依赖日报构建时临时推断账户类型。
     */
    private List<AccountOpenRecordEntity> accountOpenRecordList;

    /**
     * 当前电表快照。
     * 仅在“开户首日只有开户事件，缺少当日上报快照、销户快照和前一日报”时，
     * 作为 meterName、deviceNo、spaceId、spaceName 的兜底来源。
     * 这个字段不用于推断 electricAccountType，也不用于 owner 快照。
     */
    private List<MeterSnapshotSourceQo> currentMeterSnapshotList;
}
