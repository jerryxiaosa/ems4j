package info.zhihui.ems.components.translate.formatter;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("MoneyScale2TextFormatter测试")
class MoneyScale2TextFormatterTest {

    private final MoneyScale2TextFormatter formatter = new MoneyScale2TextFormatter();

    @Test
    @DisplayName("源值为空时应返回null")
    void testFormat_NullSourceValue_ShouldReturnNull() {
        String result = formatter.format(null, new TranslateContext());

        assertNull(result);
    }

    @Test
    @DisplayName("BigDecimal源值应按四舍五入保留两位小数")
    void testFormat_BigDecimalSource_ShouldRoundToScale2() {
        String result = formatter.format(new BigDecimal("12.345"), new TranslateContext());

        assertEquals("12.35", result);
    }

    @Test
    @DisplayName("数值字符串源值应格式化为两位小数")
    void testFormat_StringNumberSource_ShouldFormatToScale2() {
        String result = formatter.format("12", new TranslateContext());

        assertEquals("12.00", result);
    }

    @Test
    @DisplayName("非法数值源值应返回null")
    void testFormat_InvalidSource_ShouldReturnNull() {
        String result = formatter.format(new Object(), new TranslateContext());

        assertNull(result);
    }
}
