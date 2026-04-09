package info.zhihui.ems.business.report.repository.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.qo.ElectricBillMeterCountQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 电表日报数据访问层
 */
@Repository
public interface DailyMeterReportRepository extends BaseMapper<DailyMeterReportEntity> {

    /**
     * 根据报表日期删除电表日报。
     *
     * @param reportDate 报表日期
     * @return 删除条数
     */
    int deleteByReportDate(LocalDate reportDate);

    /**
     * 根据报表日期查询电表日报列表。
     *
     * @param reportDate 报表日期
     * @return 电表日报列表
     */
    List<DailyMeterReportEntity> findListByReportDate(LocalDate reportDate);

    /**
     * 根据报表日期和账户ID列表查询仍需承接的电表日报。
     *
     * @param reportDate 报表日期
     * @param accountIdList 账户ID列表
     * @return 电表日报列表
     */
    List<DailyMeterReportEntity> findActiveListByReportDateAndAccountIdList(@Param("reportDate") LocalDate reportDate,
                                                                            @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 查询账户在统计区间内的去重电表数量。
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param accountIdList 账户ID列表
     * @return 电表数量列表
     */
    List<ElectricBillMeterCountQo> findMeterCountListByAccountIdList(@Param("startDate") LocalDate startDate,
                                                                     @Param("endDate") LocalDate endDate,
                                                                     @Param("accountIdList") List<Integer> accountIdList);

    /**
     * 查询单个账户在统计区间内的电表日报列表。
     *
     * @param accountId 账户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 电表日报列表
     */
    List<DailyMeterReportEntity> findListByAccountIdAndDateRange(@Param("accountId") Integer accountId,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);
}
