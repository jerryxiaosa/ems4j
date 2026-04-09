package info.zhihui.ems.business.report.repository.source;

import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.billing.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.OrderFlowEntity;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户级日报源事实查询仓储
 */
@Repository
public interface ReportAccountSourceRepository {

    /**
     * 按账户批次查询单日报表区间内的包月扣费流水。
     *
     * @param query 日期范围
     * @param monthlyConsumeType 包月消费类型
     * @param accountIdList 账户ID列表
     * @return 包月扣费流水列表
     */
    List<AccountBalanceConsumeRecordEntity> findDailyMonthlyConsumeRecordListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                                              @Param("monthlyConsumeType") Integer monthlyConsumeType,
                                                                                              @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 查询单日报表区间内涉及账户余额流水的账户ID列表。
     *
     * @param query 日期范围
     * @return 账户ID列表
     */
    List<Integer> findDailyAccountOrderFlowAccountIdList(ReportDateRangeQo query);

    /**
     * 查询指定时间点之前已全部销户的账户ID列表。
     *
     * @param beginTime 统计日开始时间
     * @param accountIdList 账户ID列表
     * @return 已全部销户的账户ID列表
     */
    List<Integer> findFullCancelledAccountIdListBeforeTime(@Param("beginTime") LocalDateTime beginTime,
                                                           @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 按账户批次查询单日报表区间内的账户余额流水。
     *
     * @param query 日期范围
     * @param balanceType 余额类型
     * @param accountIdList 账户ID列表
     * @return 账户余额流水列表
     */
    List<OrderFlowEntity> findDailyAccountOrderFlowListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                       @Param("balanceType") Integer balanceType,
                                                                       @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 按账户批次查询账户开户快照。
     *
     * @param accountIdList 账户ID列表
     * @return 账户开户快照列表
     */
    List<AccountOpenRecordEntity> findAccountOpenRecordListByAccountIdList(@Param("accountIdList") List<Integer> accountIdList);
}
