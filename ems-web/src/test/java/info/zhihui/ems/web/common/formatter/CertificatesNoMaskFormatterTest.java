package info.zhihui.ems.web.common.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("CertificatesNoMaskFormatter测试")
class CertificatesNoMaskFormatterTest {

    private final CertificatesNoMaskFormatter formatter = new CertificatesNoMaskFormatter();

    @Test
    @DisplayName("身份证号应按长度规则脱敏")
    void testFormat_IdCard_ShouldMaskByLengthRule() {
        String result = formatter.format("110101199001011234", null);

        assertEquals("110***********1234", result);
    }

    @Test
    @DisplayName("护照号应按长度规则脱敏")
    void testFormat_Passport_ShouldMaskByLengthRule() {
        String result = formatter.format("E1234567", null);

        assertEquals("E1****67", result);
    }

    @Test
    @DisplayName("短证件号应保留前一后一")
    void testFormat_ShortCertificatesNo_ShouldKeepFirstOneAndLastOne() {
        String result = formatter.format("ABC123", null);

        assertEquals("A****3", result);
    }

    @Test
    @DisplayName("两位证件号应全部脱敏")
    void testFormat_TwoCharacterCertificatesNo_ShouldFullyMask() {
        String result = formatter.format("AB", null);

        assertEquals("**", result);
    }

    @Test
    @DisplayName("空字符串应原样返回")
    void testFormat_EmptyCertificatesNo_ShouldReturnEmptyString() {
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
