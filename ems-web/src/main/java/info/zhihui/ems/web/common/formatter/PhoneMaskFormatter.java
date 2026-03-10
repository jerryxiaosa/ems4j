package info.zhihui.ems.web.common.formatter;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.components.translate.formatter.FieldTextFormatter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 手机号脱敏格式化器。
 */
@Component
public class PhoneMaskFormatter implements FieldTextFormatter {

    @Override
    public String format(Object sourceValue, TranslateContext context) {
        if (!(sourceValue instanceof String phone)) {
            return null;
        }
        if (!StringUtils.hasLength(phone)) {
            return phone;
        }

        int phoneLength = phone.length();
        if (phoneLength == 1) {
            return "*";
        }

        int prefixLength = phoneLength >= 7 ? 3 : Math.min(2, phoneLength - 1);
        int suffixLength;
        if (phoneLength >= 8) {
            suffixLength = 4;
        } else if (phoneLength == 7) {
            suffixLength = 3;
        } else {
            suffixLength = 1;
        }
        int maskLength = phoneLength - prefixLength - suffixLength;
        if (maskLength <= 0) {
            return "*".repeat(phoneLength);
        }

        return phone.substring(0, prefixLength)
                + "*".repeat(maskLength)
                + phone.substring(phoneLength - suffixLength);
    }
}
