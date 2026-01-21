package info.zhihui.ems.common.enums;

import lombok.Getter;

@Getter
public enum DeviceTypeEnum {
    // key 对应的是 device_type表里的type_key
    ELECTRIC("electricMeter", 1),

    WATER("waterMeter", 2),

    GATEWAY("gateway", null),
    ;


    private final String key;
    private final Integer meterTypeCode;

    DeviceTypeEnum(String key, Integer meterTypeCode) {
        this.key = key;
        this.meterTypeCode = meterTypeCode;
    }

    public static DeviceTypeEnum fromKey(String key) {
        for (DeviceTypeEnum value : values()) {
            if (value.key.equals(key)) {
                return value;
            }
        }
        return null;
    }

}
