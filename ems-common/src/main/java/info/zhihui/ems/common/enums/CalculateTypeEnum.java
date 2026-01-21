package info.zhihui.ems.common.enums;

import lombok.Getter;

/**
 * @author jerryxiaosa
 */
@Getter
public enum CalculateTypeEnum implements CodeEnum<Integer> {
    AIR_CONDITIONING(1, "空调"),
    ELEVATOR(2, "电梯"),
    PUBLIC_LIGHTING(3, "公共照明"),
    OFFICE_LIGHTING(4, "办公照明");

    private final Integer code;
    private final String info;

    CalculateTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
