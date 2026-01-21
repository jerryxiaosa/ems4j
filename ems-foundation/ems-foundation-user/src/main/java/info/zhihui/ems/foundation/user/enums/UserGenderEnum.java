package info.zhihui.ems.foundation.user.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * 用户性别枚举
 */
@Getter
public enum UserGenderEnum implements CodeEnum<Integer> {
    MALE(1),
    FEMALE(2),
    ;

    private final Integer code;

    UserGenderEnum(Integer code) {
        this.code = code;
    }
}