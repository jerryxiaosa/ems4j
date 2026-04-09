package info.zhihui.ems.business.report.qo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ReportDateRangeQo {

    @NotNull
    private LocalDate reportDate;

    private LocalDate previousReportDate;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;
}
