package info.zhihui.ems.business.plan.util;

import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarnTypeCalculatorTest {

    @Test
    void testCompute_BalanceIsNull_ReturnNone() {
        WarnTypeEnum result = WarnTypeCalculator.compute(null,
                new BigDecimal("10"), new BigDecimal("5"));

        assertEquals(WarnTypeEnum.NONE, result);
    }

    @Test
    void testCompute_FirstAndSecondAreNull_ReturnNone() {
        WarnTypeEnum result = WarnTypeCalculator.compute(new BigDecimal("8"), null, null);

        assertEquals(WarnTypeEnum.NONE, result);
    }

    @Test
    void testCompute_BalanceLessThanOrEqualSecond_ReturnSecond() {
        WarnTypeEnum result = WarnTypeCalculator.compute(new BigDecimal("4.99"),
                new BigDecimal("10"), new BigDecimal("5"));

        assertEquals(WarnTypeEnum.SECOND, result);
    }

    @Test
    void testCompute_BalanceLessThanOrEqualFirst_ReturnFirst() {
        WarnTypeEnum result = WarnTypeCalculator.compute(new BigDecimal("9"),
                new BigDecimal("10"), new BigDecimal("5"));

        assertEquals(WarnTypeEnum.FIRST, result);
    }

    @Test
    void testCompute_BalanceEqualsSecondBoundary_ReturnSecond() {
        WarnTypeEnum result = WarnTypeCalculator.compute(new BigDecimal("5"),
                new BigDecimal("10"), new BigDecimal("5"));

        assertEquals(WarnTypeEnum.SECOND, result);
    }

    @Test
    void testCompute_BalanceEqualsFirstBoundary_ReturnFirst() {
        WarnTypeEnum result = WarnTypeCalculator.compute(new BigDecimal("10"),
                new BigDecimal("10"), new BigDecimal("5"));

        assertEquals(WarnTypeEnum.FIRST, result);
    }

    @Test
    void testCompute_WithWarnPlanBoOverload_ReturnSameRule() {
        WarnPlanBo warnPlanBo = new WarnPlanBo()
                .setFirstLevel(new BigDecimal("10"))
                .setSecondLevel(new BigDecimal("5"));

        WarnTypeEnum result = WarnTypeCalculator.compute(new BigDecimal("4"), warnPlanBo);

        assertEquals(WarnTypeEnum.SECOND, result);
    }

    @Test
    void testCompute_WithNullWarnPlanBo_ThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> WarnTypeCalculator.compute(new BigDecimal("1"), (WarnPlanBo) null));
    }
}
