package info.zhihui.ems.business.report.repository.source;

import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 充值类日报源事实查询仓储
 */
@Repository
public interface ReportRechargeSourceRepository {

    /**
     * 按账户批次查询单日报表区间内的电表充值到账记录。
     *
     * @param query 日期范围
     * @param orderType 订单类型
     * @param orderStatus 订单状态
     * @param meterBalanceType 电表余额类型
     * @param accountIdList 账户ID列表
     * @return 电表充值记录列表
     */
    List<RechargeSourceItemQo> findDailyMeterRechargeListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                         @Param("orderType") Integer orderType,
                                                                         @Param("orderStatus") String orderStatus,
                                                                         @Param("meterBalanceType") Integer meterBalanceType,
                                                                         @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 按账户批次查询单日报表区间内的账户充值到账记录。
     *
     * @param query 日期范围
     * @param orderType 订单类型
     * @param orderStatus 订单状态
     * @param accountIdList 账户ID列表
     * @return 账户充值记录列表
     */
    List<RechargeSourceItemQo> findDailyAccountRechargeListByAccountIdList(@Param("query") ReportDateRangeQo query,
                                                                           @Param("orderType") Integer orderType,
                                                                           @Param("orderStatus") String orderStatus,
                                                                           @Param("accountIdList") List<Integer> accountIdList);

}
