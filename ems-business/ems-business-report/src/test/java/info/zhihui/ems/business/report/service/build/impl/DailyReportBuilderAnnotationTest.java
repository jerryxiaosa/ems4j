package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.report.dto.DailyAccountBuildContextDto;
import info.zhihui.ems.business.report.dto.DailyMeterBuildContextDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DailyReportBuilderAnnotationTest {

    @Test
    void testDailyMeterReportBuilder_ShouldAddValidatedAndMethodParameterAnnotations() throws NoSuchMethodException {
        Validated validated = DailyMeterReportBuilder.class.getAnnotation(Validated.class);
        Method buildMethod = DailyMeterReportBuilder.class.getDeclaredMethod("buildDailyMeterReportList", DailyMeterBuildContextDto.class);

        assertNotNull(validated);
        assertNotNull(buildMethod.getParameters()[0].getAnnotation(NotNull.class));
        assertNotNull(buildMethod.getParameters()[0].getAnnotation(Valid.class));
    }

    @Test
    void testDailyAccountReportBuilder_ShouldAddValidatedAndMethodParameterAnnotations() throws NoSuchMethodException {
        Validated validated = DailyAccountReportBuilder.class.getAnnotation(Validated.class);
        Method buildMethod = DailyAccountReportBuilder.class.getDeclaredMethod("buildDailyAccountReportList", DailyAccountBuildContextDto.class);

        assertNotNull(validated);
        assertNotNull(buildMethod.getParameters()[0].getAnnotation(NotNull.class));
        assertNotNull(buildMethod.getParameters()[0].getAnnotation(Valid.class));
    }
}
