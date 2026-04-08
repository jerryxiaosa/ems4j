package info.zhihui.ems.schedule.task.business.report;

import info.zhihui.ems.business.report.dto.DailyReportBuildRequestDto;
import info.zhihui.ems.business.report.enums.ReportTriggerTypeEnum;
import info.zhihui.ems.business.report.service.build.DailyReportBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportBuildTask {

    static final String BUILD_CRON = "0 30 1 * * ?";

    private final DailyReportBuildService dailyReportBuildService;

    /**
     * 每天凌晨 1:30 构建前一自然日的日报。
     */
    @Scheduled(cron = BUILD_CRON)
    public void buildYesterdayDailyReport() {
        LocalDate reportDate = LocalDate.now().minusDays(1);
        log.info("开始构建日报，reportDate={}", reportDate);
        dailyReportBuildService.buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(reportDate)
                .setEndDate(reportDate)
                .setTriggerType(ReportTriggerTypeEnum.SCHEDULED.getCode())
                .setTriggerBy("schedule"));
    }
}
