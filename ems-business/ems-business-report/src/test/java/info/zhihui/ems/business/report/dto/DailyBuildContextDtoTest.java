package info.zhihui.ems.business.report.dto;

import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DailyBuildContextDtoTest {

    @Test
    void testDailyBuildContextDto_ShouldRequireReportDateRange() throws NoSuchFieldException {
        Field meterReportDateRangeField = DailyMeterBuildContextDto.class.getDeclaredField("reportDateRange");
        Field accountReportDateRangeField = DailyAccountBuildContextDto.class.getDeclaredField("reportDateRange");

        assertNotNull(meterReportDateRangeField.getAnnotation(NotNull.class));
        assertNotNull(meterReportDateRangeField.getAnnotation(Valid.class));
        assertNotNull(accountReportDateRangeField.getAnnotation(NotNull.class));
        assertNotNull(accountReportDateRangeField.getAnnotation(Valid.class));
    }

    @Test
    void testReportDateRangeQo_ShouldRequireReportDate() throws NoSuchFieldException {
        Field reportDateField = ReportDateRangeQo.class.getDeclaredField("reportDate");

        assertNotNull(reportDateField.getAnnotation(NotNull.class));
    }
}
