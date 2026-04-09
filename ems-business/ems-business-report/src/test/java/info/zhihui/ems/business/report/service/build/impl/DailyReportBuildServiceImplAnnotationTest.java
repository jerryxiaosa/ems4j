package info.zhihui.ems.business.report.service.build.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("日报统一构建服务注解测试")
class DailyReportBuildServiceImplAnnotationTest {

    @Test
    @DisplayName("构建服务应开启方法级校验")
    void testDailyReportBuildServiceImpl_ShouldAddValidatedAnnotation() {
        Validated validated = DailyReportBuildServiceImpl.class.getAnnotation(Validated.class);

        assertNotNull(validated);
    }
}
