package info.zhihui.ems.web.common.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("PowerScale2TextFormatter测试")
class PowerScale2TextFormatterTest {

    private final PowerScale2TextFormatter formatter = new PowerScale2TextFormatter();

    @Test
    @DisplayName("源值为空时应返回null")
    void testFormat_NullSourceValue_ShouldReturnNull() {
        String result = formatter.format(null, null);

        assertNull(result);
    }

    @Test
    @DisplayName("电量应保留最多两位小数并去掉尾零")
    void testFormat_BigDecimalSource_ShouldKeepScale2AndStripTrailingZero() {
        String result = formatter.format(new BigDecimal("12.300"), null);

        assertEquals("12.3", result);
    }

    @Test
    @DisplayName("整数电量应输出整数文本")
    void testFormat_IntegerPower_ShouldReturnIntegerText() {
        String result = formatter.format(new BigDecimal("50.00"), null);

        assertEquals("50", result);
    }

    @Test
    @DisplayName("非法数值源值应返回null")
    void testFormat_InvalidSource_ShouldReturnNull() {
        String result = formatter.format(new Object(), null);

        assertNull(result);
    }
}
