package info.zhihui.ems.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 尖峰平谷深谷
 */
@Getter
public enum ElectricPricePeriodEnum {
    TOTAL(0),
    HIGHER(1),
    HIGH(2),
    LOW(3),
    LOWER(4),
    DEEP_LOW(5);

    private final Integer code;

    ElectricPricePeriodEnum(Integer code) {
        this.code = code;
    }

    @JsonValue
    public Integer getJsonValue() {
        return this.code;
    }

    @JsonCreator
    public static ElectricPricePeriodEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ElectricPricePeriodEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported ElectricDegreeTypeEnum code: " + code);
    }
}
