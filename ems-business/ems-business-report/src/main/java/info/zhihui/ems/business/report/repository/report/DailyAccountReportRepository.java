package info.zhihui.ems.business.report.repository.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
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
}
