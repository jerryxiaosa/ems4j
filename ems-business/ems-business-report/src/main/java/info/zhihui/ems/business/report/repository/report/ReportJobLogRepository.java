package info.zhihui.ems.business.report.repository.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.report.entity.ReportJobLogEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日报任务日志数据访问层
 */
@Repository
public interface ReportJobLogRepository extends BaseMapper<ReportJobLogEntity> {

    /**
     * 更新任务运行状态和当前执行日期。
     *
     * @param id 日志ID
     * @param status 任务状态
     * @param currentReportDate 当前执行日期
     * @return 更新条数
     */
    int updateStatus(@Param("id") Integer id,
                     @Param("status") Integer status,
                     @Param("currentReportDate") LocalDate currentReportDate);

    /**
     * 更新任务失败状态。
     *
     * @param id 日志ID
     * @param status 任务状态
     * @param currentReportDate 当前执行日期
     * @param errorMessage 错误信息
     * @param finishTime 完成时间
     * @return 更新条数
     */
    int updateFailure(@Param("id") Integer id,
                      @Param("status") Integer status,
                      @Param("currentReportDate") LocalDate currentReportDate,
                      @Param("errorMessage") String errorMessage,
                      @Param("finishTime") LocalDateTime finishTime);

    /**
     * 更新任务成功状态。
     *
     * @param id 日志ID
     * @param status 任务状态
     * @param finishTime 完成时间
     * @return 更新条数
     */
    int updateSuccess(@Param("id") Integer id,
                      @Param("status") Integer status,
                      @Param("finishTime") LocalDateTime finishTime);
}
