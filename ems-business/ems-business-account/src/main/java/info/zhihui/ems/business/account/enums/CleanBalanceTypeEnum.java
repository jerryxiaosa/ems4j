package info.zhihui.ems.business.account.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * @author jerryxiaosa
 * 销户结算类型
 */
@Getter
public enum CleanBalanceTypeEnum implements CodeEnum<Integer> {
    // 无处理
    SKIP(0, "无费用"),

    // 退费
    REFUND(1, "退款"),

    // 补缴
    PAY(2, "补缴"),
    ;

    private final Integer code;
    private final String info;

    CleanBalanceTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
