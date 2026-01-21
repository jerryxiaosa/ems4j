package info.zhihui.ems.foundation.user.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * 菜单来源枚举
 */
@Getter
public enum MenuSourceEnum implements CodeEnum<Integer> {
    WEB(1, "Web端"),
    MOBILE(2, "移动端"),
    ;

    private final Integer code;
    private final String info;

    MenuSourceEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}