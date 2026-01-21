package info.zhihui.ems.foundation.integration.core.enums;

import lombok.Getter;

/**
 * 系统模块的枚举
 *
 * @author jerryxiaosa
 */

@Getter
public enum ModuleEnum {

    ENERGY("energy"),
    ;

    private final String moduleName;

    ModuleEnum(String moduleName) {
        this.moduleName = moduleName;
    }

}
