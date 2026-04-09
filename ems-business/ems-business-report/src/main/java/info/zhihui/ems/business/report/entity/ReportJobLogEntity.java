package info.zhihui.ems.business.report.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("energy_report_job_log")
public class ReportJobLogEntity extends BaseEntity {

    private Integer id;

    private Integer triggerType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer status;

    @TableField("current_report_date")
    private LocalDate currentReportDate;

    private String errorMessage;

    private String triggerBy;

    private LocalDateTime finishTime;
}
