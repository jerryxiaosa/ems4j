package info.zhihui.ems.common.enums;

import lombok.Getter;

@Getter
public enum ElectricAccountTypeEnum implements CodeEnum<Integer> {
    QUANTITY(0, "按需计费"),
    MONTHLY(1, "包月计费"),
    MERGED(2, "合并按需计费")
    ;

    private final Integer code;

    private final String info;

    ElectricAccountTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

}
