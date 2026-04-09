package info.zhihui.ems.business.report.repository.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.qo.ElectricBillAccountSummaryQo;
import info.zhihui.ems.business.report.qo.ElectricBillReportQueryQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 账户日报数据访问层
 */
@Repository
public interface DailyAccountReportRepository extends BaseMapper<DailyAccountReportEntity> {

    /**
     * 根据报表日期删除账户日报。
     *
     * @param reportDate 报表日期
     * @return 删除条数
     */
    int deleteByReportDate(LocalDate reportDate);

    /**
     * 根据报表日期查询账户日报列表。
     *
     * @param reportDate 报表日期
     * @return 账户日报列表
     */
    List<DailyAccountReportEntity> findListByReportDate(LocalDate reportDate);

    /**
     * 查询指定报表日期下涉及的账户ID列表。
     *
     * @param reportDate 报表日期
     * @return 账户ID列表
     */
    List<Integer> findAccountIdListByReportDate(LocalDate reportDate);

    /**
     * 根据报表日期和账户ID列表查询账户日报。
     *
     * @param reportDate 报表日期
     * @param accountIdList 账户ID列表
     * @return 账户日报列表
     */
    List<DailyAccountReportEntity> findListByReportDateAndAccountIdList(@Param("reportDate") LocalDate reportDate,
                                                                        @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 分页查询电费报表账户汇总列表。
     *
     * @param queryQo 查询条件
     * @return 汇总列表
     */
    List<ElectricBillAccountSummaryQo> findElectricBillAccountPageList(ElectricBillReportQueryQo queryQo);

    /**
     * 查询单个账户在统计区间内的电费报表汇总。
     *
     * @param accountId 账户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 汇总结果
     */
    ElectricBillAccountSummaryQo getElectricBillAccountSummary(@Param("accountId") Integer accountId,
                                                               @Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);

    /**
     * 查询账户在统计区间内最后一条日报。
     *
     * @param accountId 账户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 最后一条日报
     */
    DailyAccountReportEntity getLatestByAccountIdAndDateRange(@Param("accountId") Integer accountId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
}
