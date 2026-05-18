package info.zhihui.ems.common.utils;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * BigDecimal 工具类。
 */
public final class BigDecimalUtils {

    private BigDecimalUtils() {
    }

    /**
     * 将空值转换为 BigDecimal.ZERO。
     *
     * @param value 原始数值
     * @return 非空数值，空值返回 BigDecimal.ZERO
     */
    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 累加数值集合，忽略空元素。
     *
     * @param values 数值集合
     * @return 累加结果
     */
    public static BigDecimal sumIgnoreNull(Collection<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            result = result.add(zeroIfNull(value));
        }
        return result;
    }
}
