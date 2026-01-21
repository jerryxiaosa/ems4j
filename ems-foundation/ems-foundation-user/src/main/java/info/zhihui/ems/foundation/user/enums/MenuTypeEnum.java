package info.zhihui.ems.foundation.user.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * 菜单类型枚举
 */
@Getter
public enum MenuTypeEnum implements CodeEnum<Integer> {
    MENU(1, "菜单"),
    BUTTON(2, "按钮"),
    ;

    private final Integer code;
    private final String info;

    MenuTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}