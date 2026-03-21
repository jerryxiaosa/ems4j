package info.zhihui.ems.foundation.integration.biz.command.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;


@Getter
public enum CommandTypeEnum implements CodeEnum<Integer> {
    ENERGY_ELECTRIC_TURN_ON(1, "电表合闸", false),
    ENERGY_ELECTRIC_TURN_OFF(2, "电表断闸", false),
    ENERGY_ELECTRIC_PRICE_TIME(3, "下发尖峰平谷时间段", true),
    ENERGY_ELECTRIC_DATE_DURATION(4, "下发指定日期电价方案", true),
    ENERGY_ELECTRIC_CT(5, "设置CT变比", false),
    ;

    private final Integer code;

    private final String info;

    private final boolean retryable;

    CommandTypeEnum(Integer code, String info, boolean retryable) {
        this.code = code;
        this.info = info;
        this.retryable = retryable;
    }
}
