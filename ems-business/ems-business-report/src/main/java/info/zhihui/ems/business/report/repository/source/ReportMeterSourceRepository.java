package info.zhihui.ems.business.report.repository.source;

import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.entity.OpenMeterEntity;
import info.zhihui.ems.business.report.qo.MeterSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.PowerRecordSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 电表级日报源事实查询仓储
 */
@Repository
public interface ReportMeterSourceRepository {

    /**
     * 查询单日报表区间内涉及上报的账户ID列表。
     *
     * @param query 日期范围
     * @return 账户ID列表
     */
    List<Integer> findDailyPowerRecordAccountIdList(ReportDateRangeQo query);

    /**
     * 按账户批次查询单日报表区间内的电表上报快照。
     *
     * @param query 日期范围
     * @param accountIdList 账户ID列表
     * @return 电表上报快照列表
     */
    List<PowerRecordSnapshotSourceQo> findDailyPowerRecordSnapshotListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                                       @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 按账户批次查询当前电表快照。
     *
     * @param accountIdList 账户ID列表
     * @return 当前电表快照列表
     */
    List<MeterSnapshotSourceQo> findCurrentMeterSnapshotListByAccountIdList(@Param("accountIdList") List<Integer> accountIdList);

    /**
     * 按账户批次查询单日报表区间内的电表余额消费流水。
     *
     * @param query 日期范围
     * @param consumeType 消费类型
     * @param accountIdList 账户ID列表
     * @return 余额消费流水列表
     */
    List<ElectricMeterBalanceConsumeRecordEntity> findDailyBalanceConsumeRecordListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                                                    @Param("consumeType") Integer consumeType,
                                                                                                    @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 查询单日报表区间内涉及开户的账户ID列表。
     *
     * @param query 日期范围
     * @param meterType 电表类型
     * @return 账户ID列表
     */
    List<Integer> findDailyOpenAccountIdList(@Param("query") ReportDateRangeQo query,
                                             @Param("meterType") Integer meterType);

    /**
     * 按账户批次查询单日报表区间内的开户记录。
     *
     * @param query 日期范围
     * @param meterType 电表类型
     * @param accountIdList 账户ID列表
     * @return 开户记录列表
     */
    List<OpenMeterEntity> findDailyOpenRecordListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                 @Param("meterType") Integer meterType,
                                                                 @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 查询单日报表区间内涉及销户的账户ID列表。
     *
     * @param query 日期范围
     * @param meterType 电表类型
     * @return 账户ID列表
     */
    List<Integer> findDailyCancelAccountIdList(@Param("query") ReportDateRangeQo query,
                                               @Param("meterType") Integer meterType);

    /**
     * 按账户批次查询单日报表区间内的销户记录。
     *
     * @param query 日期范围
     * @param meterType 电表类型
     * @param accountIdList 账户ID列表
     * @return 销户记录列表
     */
    List<MeterCancelRecordEntity> findDailyCancelRecordListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                            @Param("meterType") Integer meterType,
                                                                            @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 按账户批次查询单日报表区间内的用电区间记录。
     *
     * @param query 日期范围
     * @param accountIdList 账户ID列表
     * @return 用电区间记录列表
     */
    List<ElectricMeterPowerConsumeRecordEntity> findDailyPowerConsumeRecordListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                                                @Param("accountIdList") List<Integer> accountIdList);
}
