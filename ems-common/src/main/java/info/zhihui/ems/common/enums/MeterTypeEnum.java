package info.zhihui.ems.common.enums;

import lombok.Getter;

/**
 * 计量设备类型枚举
 * 用于兼容现有meter_type字段
 * code与DeviceTypeEnum.meterTypeCode对应
 */
@Getter
public enum MeterTypeEnum implements CodeEnum<Integer> {
    ELECTRIC(DeviceTypeEnum.ELECTRIC.getMeterTypeCode(), "电表"),
    WATER(DeviceTypeEnum.WATER.getMeterTypeCode(), "水表");

    private final Integer code;
    private final String info;

    MeterTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}