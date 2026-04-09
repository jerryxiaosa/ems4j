package info.zhihui.ems.web.report;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.business.report.dto.DailyReportBuildRequestDto;
import info.zhihui.ems.business.report.enums.ReportTriggerTypeEnum;
import info.zhihui.ems.business.report.service.build.DailyReportBuildService;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.report.biz.DailyReportBiz;
import info.zhihui.ems.web.report.controller.DailyReportController;
import info.zhihui.ems.web.report.vo.DailyReportBuildVo;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyReportControllerTest {

    @Test
    @DisplayName("手工补算入口应委托业务层并返回成功结果")
    void testBuildDailyReport_ShouldDelegateToBiz() {
        FakeDailyReportBuildService service = new FakeDailyReportBuildService();
        DailyReportController controller = new DailyReportController(new DailyReportBiz(service));
        DailyReportBuildVo buildVo = new DailyReportBuildVo()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 7));

        RestResult<Void> result = controller.buildDailyReport(buildVo);

        assertTrue(result.getSuccess());
        assertNotNull(service.requestDto);
        assertEquals(LocalDate.of(2026, 4, 6), service.requestDto.getStartDate());
        assertEquals(LocalDate.of(2026, 4, 7), service.requestDto.getEndDate());
        assertEquals(ReportTriggerTypeEnum.MANUAL.getCode(), service.requestDto.getTriggerType());
        assertEquals("web", service.requestDto.getTriggerBy());
    }

    @Test
    @DisplayName("控制器路由与参数校验应符合约定")
    void testAnnotations_ShouldMatchExpectedContract() throws NoSuchMethodException, NoSuchFieldException {
        RequestMapping classRequestMapping = DailyReportController.class.getAnnotation(RequestMapping.class);
        Method method = DailyReportController.class.getMethod("buildDailyReport", DailyReportBuildVo.class);
        Field startDateField = DailyReportBuildVo.class.getDeclaredField("startDate");
        Field endDateField = DailyReportBuildVo.class.getDeclaredField("endDate");

        assertNotNull(classRequestMapping);
        assertEquals(ApiPathConstant.V1 + "/report/daily", classRequestMapping.value()[0]);
        assertEquals("/build", method.getAnnotation(PostMapping.class).value()[0]);
        assertEquals("reports:daily:build", method.getAnnotation(SaCheckPermission.class).value()[0]);
        assertNotNull(startDateField.getAnnotation(NotNull.class));
        assertNotNull(endDateField.getAnnotation(NotNull.class));
    }

    private static class FakeDailyReportBuildService implements DailyReportBuildService {
        private DailyReportBuildRequestDto requestDto;

        @Override
        public void buildDailyReport(DailyReportBuildRequestDto buildRequest) {
            this.requestDto = buildRequest;
        }
    }
}
