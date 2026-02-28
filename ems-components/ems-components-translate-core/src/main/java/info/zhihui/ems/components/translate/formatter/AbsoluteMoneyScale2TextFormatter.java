package info.zhihui.ems.components.translate.formatter;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额展示格式化器：取绝对值后保留两位小数
 */
@Slf4j
@Component
public class AbsoluteMoneyScale2TextFormatter implements FieldTextFormatter {

    @Override
    public String format(Object sourceValue, TranslateContext context) {
        if (sourceValue == null) {
            return null;
        }
        BigDecimal amount = toBigDecimal(sourceValue);
        if (amount == null) {
            log.warn("金额格式化失败，源值不是有效数字。type={}", sourceValue.getClass().getName());
            return null;
        }
        return amount.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal toBigDecimal(Object sourceValue) {
        if (sourceValue instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        try {
            return new BigDecimal(String.valueOf(sourceValue));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
