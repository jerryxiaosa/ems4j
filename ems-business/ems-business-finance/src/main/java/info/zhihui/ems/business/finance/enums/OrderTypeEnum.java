package info.zhihui.ems.business.finance.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * @author jerryxiaosa
 */
@Getter
public enum OrderTypeEnum implements CodeEnum<Integer> {
    ENERGY_TOP_UP(1, "能耗充值"),
    ACCOUNT_TERMINATION_SETTLEMENT(2, "账户终止结算"),
    ;

    private final Integer code;

    private final String info;

    OrderTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
