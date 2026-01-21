package info.zhihui.ems.foundation.integration.biz.command.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

@Getter
public enum CommandSourceEnum implements CodeEnum<Integer> {
    SYSTEM(0, "系统命令"),
    USER(1, "用户命令"),
    ;

    private final Integer code;

    private final String info;

    CommandSourceEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
