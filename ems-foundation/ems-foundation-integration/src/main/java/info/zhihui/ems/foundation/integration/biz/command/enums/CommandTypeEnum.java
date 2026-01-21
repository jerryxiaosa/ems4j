package info.zhihui.ems.foundation.integration.biz.command.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;


@Getter
public enum CommandTypeEnum implements CodeEnum<Integer> {
    ENERGY_ELECTRIC_TURN_ON(1, "电表充值自动合闸"),
    ENERGY_ELECTRIC_TURN_OFF(2, "电表欠费自动断闸"),
    ENERGY_ELECTRIC_PRICE_TIME(3, "下发尖峰平谷时间段"),
    ENERGY_ELECTRIC_CT(4, "设置CT变比"),
    ;

    private final Integer code;

    private final String info;

    CommandTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
