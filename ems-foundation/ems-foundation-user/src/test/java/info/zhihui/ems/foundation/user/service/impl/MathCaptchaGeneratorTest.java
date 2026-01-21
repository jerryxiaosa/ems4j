package info.zhihui.ems.foundation.user.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MathCaptchaGenerator 单元测试")
class MathCaptchaGeneratorTest {

    private final MathCaptchaGenerator generator = new MathCaptchaGenerator();

    @RepeatedTest(10)
    @DisplayName("生成表达式包含等号和占位符")
    void testGenerateExpressionFormat() {
        String code = generator.generate();
        assertThat(code).contains("=");
        assertThat(code).endsWith("=");
        String[] parts = code.split("=");
        assertThat(parts[0].trim().split("")).hasSize(3);
    }

    @Test
    @DisplayName("verify 支持正确和错误输入")
    void testVerify() {
        String expr = "2 + 3 =";
        assertThat(generator.verify(expr, "5")).isTrue();
        assertThat(generator.verify(expr, "4")).isFalse();
        assertThat(generator.verify(expr, "abc")).isFalse();
    }
}
