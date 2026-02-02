package info.zhihui.ems.business.finance.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyUtil {
    public static BigDecimal fen2yuan(Integer fen) {
        return new BigDecimal(fen.toString()).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
    }

    public static Integer yuan2fen(BigDecimal yuan) {
        return yuan.multiply(new BigDecimal("100")).intValue();
    }

    /**
     * 保留两位小数
     * @param amount 原金额
     * @return 保留两位后的金额
     */
    public static BigDecimal scaleToCent(BigDecimal amount) {
        return amount == null ? null : amount.setScale(2, RoundingMode.DOWN);
    }
}
