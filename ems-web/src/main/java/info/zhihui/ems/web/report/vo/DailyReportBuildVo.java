package info.zhihui.ems.web.report.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class DailyReportBuildVo {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
