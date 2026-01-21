package info.zhihui.ems.business.finance.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyUtilTest {

    @Test
    void testFen2Yuan_ShouldConvertAndKeepTwoDecimals() {
        BigDecimal yuan = MoneyUtil.fen2yuan(12345);
        assertEquals(new BigDecimal("123.45"), yuan);

        BigDecimal negative = MoneyUtil.fen2yuan(-50);
        assertEquals(new BigDecimal("-0.50"), negative);
    }

    @Test
    void testYuan2Fen_ShouldConvertByHundred() {
        assertEquals(12345, MoneyUtil.yuan2fen(new BigDecimal("123.45")));
        assertEquals(-50, MoneyUtil.yuan2fen(new BigDecimal("-0.50")));
    }

    @Test
    void testScaleToCent_ShouldFloorAndHandleNull() {
        BigDecimal scaled = MoneyUtil.scaleToCent(new BigDecimal("12.349"));
        assertEquals(new BigDecimal("12.34"), scaled);

        assertNull(MoneyUtil.scaleToCent(null));
    }
}
