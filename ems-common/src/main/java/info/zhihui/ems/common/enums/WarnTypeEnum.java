package info.zhihui.ems.common.enums;

import lombok.Getter;

@Getter
public enum WarnTypeEnum implements CodeEnum<String> {
    NONE("NONE", "无预警"),
    FIRST("FIRST", "一级预警"),
    SECOND("SECOND", "二级预警");

    private final String code;

    private final String info;

    WarnTypeEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
