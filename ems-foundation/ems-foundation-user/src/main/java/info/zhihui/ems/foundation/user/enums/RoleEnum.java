package info.zhihui.ems.foundation.user.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum implements CodeEnum<String> {
    SUPER_ADMIN("super_admin", "超级管理员"),
    ;

    private final String code;
    private final String info;

}
