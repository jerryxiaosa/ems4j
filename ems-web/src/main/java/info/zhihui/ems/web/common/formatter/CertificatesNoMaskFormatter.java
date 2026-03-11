package info.zhihui.ems.web.common.formatter;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.components.translate.formatter.FieldTextFormatter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 证件号脱敏格式化器。
 */
@Component
public class CertificatesNoMaskFormatter implements FieldTextFormatter {

    @Override
    public String format(Object sourceValue, TranslateContext context) {
        if (!(sourceValue instanceof String certificatesNo)) {
            return null;
        }
        if (!StringUtils.hasLength(certificatesNo)) {
            return certificatesNo;
        }

        int certificatesNoLength = certificatesNo.length();
        if (certificatesNoLength <= 2) {
            return "*".repeat(certificatesNoLength);
        }

        int prefixLength;
        int suffixLength;
        if (certificatesNoLength <= 6) {
            prefixLength = 1;
            suffixLength = 1;
        } else if (certificatesNoLength <= 10) {
            prefixLength = 2;
            suffixLength = 2;
        } else {
            prefixLength = 3;
            suffixLength = 4;
        }

        int maskLength = certificatesNoLength - prefixLength - suffixLength;
        return certificatesNo.substring(0, prefixLength)
                + "*".repeat(maskLength)
                + certificatesNo.substring(certificatesNoLength - suffixLength);
    }
}
