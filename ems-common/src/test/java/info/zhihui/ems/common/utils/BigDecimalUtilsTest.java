package info.zhihui.ems.common.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigDecimalUtilsTest {

    @Test
    void testZeroIfNull_WhenValueIsNull_ShouldReturnZero() {
        assertEquals(BigDecimal.ZERO, BigDecimalUtils.zeroIfNull(null));
    }

    @Test
    void testZeroIfNull_WhenValueExists_ShouldReturnOriginalValue() {
        BigDecimal amount = new BigDecimal("12.34");

        assertEquals(amount, BigDecimalUtils.zeroIfNull(amount));
    }

    @Test
    void testSumIgnoreNull_WhenValuesContainNull_ShouldIgnoreNullValue() {
        BigDecimal result = BigDecimalUtils.sumIgnoreNull(Arrays.asList(
                new BigDecimal("1.10"),
                null,
                new BigDecimal("2.30")
        ));

        assertEquals(new BigDecimal("3.40"), result);
    }
}
