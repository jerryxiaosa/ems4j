package info.zhihui.ems.components.translate.formatter;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("AbsoluteMoneyScale2TextFormatter测试")
class AbsoluteMoneyScale2TextFormatterTest {

    private final AbsoluteMoneyScale2TextFormatter formatter = new AbsoluteMoneyScale2TextFormatter();

    @Test
    @DisplayName("源值为空时应返回null")
    void testFormat_NullSourceValue_ShouldReturnNull() {
        String result = formatter.format(null, new TranslateContext());

        assertNull(result);
    }

    @Test
    @DisplayName("正数金额应格式化为两位小数")
    void testFormat_PositiveBigDecimalSource_ShouldFormatToScale2() {
        String result = formatter.format(new BigDecimal("12.345"), new TranslateContext());

        assertEquals("12.35", result);
    }

    @Test
    @DisplayName("负数金额应先取绝对值再格式化为两位小数")
    void testFormat_NegativeBigDecimalSource_ShouldFormatAbsoluteValueToScale2() {
        String result = formatter.format(new BigDecimal("-12.345"), new TranslateContext());

        assertEquals("12.35", result);
    }

    @Test
    @DisplayName("数值字符串源值应先取绝对值再格式化为两位小数")
    void testFormat_NegativeStringNumberSource_ShouldFormatAbsoluteValueToScale2() {
        String result = formatter.format("-12", new TranslateContext());

        assertEquals("12.00", result);
    }

    @Test
    @DisplayName("非法数值源值应返回null")
    void testFormat_InvalidSource_ShouldReturnNull() {
        String result = formatter.format(new Object(), new TranslateContext());

        assertNull(result);
    }
}
