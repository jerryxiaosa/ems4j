package info.zhihui.ems.web.common.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("PriceScale4TextFormatter测试")
class PriceScale4TextFormatterTest {

    private final PriceScale4TextFormatter formatter = new PriceScale4TextFormatter();

    @Test
    @DisplayName("源值为空时应返回null")
    void testFormat_NullSourceValue_ShouldReturnNull() {
        String result = formatter.format(null, null);

        assertNull(result);
    }

    @Test
    @DisplayName("单价应固定保留四位小数")
    void testFormat_BigDecimalSource_ShouldKeepScale4() {
        String result = formatter.format(new BigDecimal("0.668"), null);

        assertEquals("0.6680", result);
    }

    @Test
    @DisplayName("两位小数单价也应补足四位")
    void testFormat_Scale2Source_ShouldPadToScale4() {
        String result = formatter.format(new BigDecimal("0.90"), null);

        assertEquals("0.9000", result);
    }

    @Test
    @DisplayName("非法数值源值应返回null")
    void testFormat_InvalidSource_ShouldReturnNull() {
        String result = formatter.format(new Object(), null);

        assertNull(result);
    }
}
