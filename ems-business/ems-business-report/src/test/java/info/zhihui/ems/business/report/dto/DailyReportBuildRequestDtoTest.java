package info.zhihui.ems.business.report.dto;

import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DailyReportBuildRequestDtoTest {

    @Test
    void testDailyReportBuildRequestDto_ShouldRequireStartAndEndDate() throws NoSuchFieldException {
        Field startDateField = DailyReportBuildRequestDto.class.getDeclaredField("startDate");
        Field endDateField = DailyReportBuildRequestDto.class.getDeclaredField("endDate");

        assertNotNull(startDateField.getAnnotation(NotNull.class));
        assertNotNull(endDateField.getAnnotation(NotNull.class));
    }
}
