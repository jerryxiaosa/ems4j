package info.zhihui.ems.web.common.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("PhoneMaskFormatter测试")
class PhoneMaskFormatterTest {

    private final PhoneMaskFormatter formatter = new PhoneMaskFormatter();

    @Test
    @DisplayName("标准手机号应保留前三后四")
    void testFormat_NormalPhone_ShouldKeepFirstThreeAndLastFour() {
        String result = formatter.format("13800138000", null);

        assertEquals("138****8000", result);
    }

    @Test
    @DisplayName("短手机号也应脱敏")
    void testFormat_ShortPhone_ShouldStillMask() {
        String result = formatter.format("123456", null);

        assertEquals("12***6", result);
    }

    @Test
    @DisplayName("7位手机号应保留部分可见上下文")
    void testFormat_SevenCharacterPhone_ShouldKeepVisibleContext() {
        String result = formatter.format("1234567", null);

        assertEquals("123*567", result);
    }

    @Test
    @DisplayName("单字符手机号也应脱敏")
    void testFormat_SingleCharacterPhone_ShouldMask() {
        String result = formatter.format("1", null);

        assertEquals("*", result);
    }

    @Test
    @DisplayName("空字符串应原样返回")
    void testFormat_EmptyPhone_ShouldReturnEmptyString() {
        String result = formatter.format("", null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("非字符串输入应返回null")
    void testFormat_NonStringSource_ShouldReturnNull() {
        String result = formatter.format(123456, null);

        assertNull(result);
    }
}
