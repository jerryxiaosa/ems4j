package info.zhihui.ems.common.enums;

import lombok.Getter;

@Getter
public enum BalanceTypeEnum implements CodeEnum<Integer> {
    ACCOUNT(0, "账户余额"),
    ELECTRIC_METER(1, "电表余额"),
    ;

    private final Integer code;

    private final String info;

    BalanceTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

}
