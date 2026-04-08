package info.zhihui.ems.business.report;

import info.zhihui.ems.business.report.dto.DailyReportBuildRequestDto;
import info.zhihui.ems.business.report.service.build.DailyReportBuildService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@DisplayName("日报统一构建服务集成测试")
class DailyReportBuildServiceIntegrationTest {

    @Autowired
    private DailyReportBuildService dailyReportBuildService;

    @Test
    @DisplayName("开始日期为空应触发方法参数校验")
    void testBuildDailyReport_StartDateNull_ShouldThrowConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () -> dailyReportBuildService.buildDailyReport(
                new DailyReportBuildRequestDto()
                        .setEndDate(LocalDate.of(2026, 4, 6))
        ));
    }

    @Test
    @DisplayName("结束日期为空应触发方法参数校验")
    void testBuildDailyReport_EndDateNull_ShouldThrowConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () -> dailyReportBuildService.buildDailyReport(
                new DailyReportBuildRequestDto()
                        .setStartDate(LocalDate.of(2026, 4, 6))
        ));
    }
}
