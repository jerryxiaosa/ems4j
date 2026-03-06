package info.zhihui.ems.business.plan.util;

import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.common.enums.WarnTypeEnum;

import java.math.BigDecimal;
import java.util.Objects;

public final class WarnTypeCalculator {

    private WarnTypeCalculator() {
    }

    public static WarnTypeEnum compute(BigDecimal balance, BigDecimal firstLevel, BigDecimal secondLevel) {
        if (balance == null) {
            return WarnTypeEnum.NONE;
        }
        if (secondLevel != null && balance.compareTo(secondLevel) <= 0) {
            return WarnTypeEnum.SECOND;
        }
        if (firstLevel != null && balance.compareTo(firstLevel) <= 0) {
            return WarnTypeEnum.FIRST;
        }
        return WarnTypeEnum.NONE;
    }

    public static WarnTypeEnum compute(BigDecimal balance, WarnPlanBo warnPlanBo) {
        WarnPlanBo requiredWarnPlan = Objects.requireNonNull(warnPlanBo, "warnPlanBo cannot be null");
        return compute(balance, requiredWarnPlan.getFirstLevel(), requiredWarnPlan.getSecondLevel());
    }
}
