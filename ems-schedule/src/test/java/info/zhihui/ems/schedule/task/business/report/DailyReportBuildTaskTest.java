package info.zhihui.ems.schedule.task.business.report;

import info.zhihui.ems.business.report.dto.DailyReportBuildRequestDto;
import info.zhihui.ems.business.report.enums.ReportTriggerTypeEnum;
import info.zhihui.ems.business.report.service.build.DailyReportBuildService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DailyReportBuildTaskTest {

    @Test
    @DisplayName("日报任务应构建前一自然日并标记为SCHEDULED")
    void testBuildYesterdayDailyReport_ShouldUseYesterdayRange() {
        FakeDailyReportBuildService service = new FakeDailyReportBuildService();
        DailyReportBuildTask task = new DailyReportBuildTask(service);

        task.buildYesterdayDailyReport();

        assertNotNull(service.requestDto);
        assertEquals(LocalDate.now().minusDays(1), service.requestDto.getStartDate());
        assertEquals(LocalDate.now().minusDays(1), service.requestDto.getEndDate());
        assertEquals(ReportTriggerTypeEnum.SCHEDULED.getCode(), service.requestDto.getTriggerType());
        assertEquals("schedule", service.requestDto.getTriggerBy());
    }

    @Test
    @DisplayName("日报任务cron应符合约定")
    void testScheduledAnnotation_ShouldMatchExpectedCron() throws NoSuchMethodException {
        Method method = DailyReportBuildTask.class.getMethod("buildYesterdayDailyReport");

        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertNotNull(scheduled);
        assertEquals("0 30 1 * * ?", scheduled.cron());
    }

    private static class FakeDailyReportBuildService implements DailyReportBuildService {
        private DailyReportBuildRequestDto requestDto;

        @Override
        public void buildDailyReport(DailyReportBuildRequestDto buildRequest) {
            this.requestDto = buildRequest;
        }
    }
}
