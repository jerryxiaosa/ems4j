package info.zhihui.ems.web.common.formatter;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.components.translate.formatter.FieldTextFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 电量展示格式化器：最多保留两位小数，去掉无意义尾零。
 */
@Slf4j
@Component
public class PowerScale2TextFormatter implements FieldTextFormatter {

    @Override
    public String format(Object sourceValue, TranslateContext context) {
        if (sourceValue == null) {
            return null;
        }
        BigDecimal powerAmount = toBigDecimal(sourceValue);
        if (powerAmount == null) {
            log.warn("电量格式化失败，源值不是有效数字。type={}", sourceValue.getClass().getName());
            return null;
        }
        BigDecimal normalizedAmount = normalizeZero(powerAmount).setScale(2, RoundingMode.HALF_UP);
        return normalizedAmount.stripTrailingZeros().toPlainString();
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
