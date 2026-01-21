package info.zhihui.ems.business.device.enums;

import lombok.Getter;

@Getter
public enum ElectricSwitchStatusEnum {
    ON(0, "合闸"),
    OFF(1, "断闸"),
    ;

    private final Integer code;

    private final String info;

    ElectricSwitchStatusEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
