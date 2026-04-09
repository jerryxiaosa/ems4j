package info.zhihui.ems.web.common.formatter;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.components.translate.formatter.FieldTextFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 单价展示格式化器：固定保留四位小数。
 */
@Slf4j
@Component
public class PriceScale4TextFormatter implements FieldTextFormatter {

    @Override
    public String format(Object sourceValue, TranslateContext context) {
        if (sourceValue == null) {
            return null;
        }
        BigDecimal priceAmount = toBigDecimal(sourceValue);
        if (priceAmount == null) {
            log.warn("单价格式化失败，源值不是有效数字。type={}", sourceValue.getClass().getName());
            return null;
        }
        return normalizeZero(priceAmount).setScale(4, RoundingMode.HALF_UP).toPlainString();
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

    private BigDecimal normalizeZero(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : amount;
    }
}
