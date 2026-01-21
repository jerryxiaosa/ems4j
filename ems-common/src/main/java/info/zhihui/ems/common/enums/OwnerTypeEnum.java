package info.zhihui.ems.common.enums;

import lombok.Getter;

/**
 * @author jerryxiaosa
 * 账户类型
 */
@Getter
public enum OwnerTypeEnum implements CodeEnum<Integer> {
    ENTERPRISE(0),
    PERSONAL(1),
    ;

    private final Integer code;

    OwnerTypeEnum(Integer code) {
        this.code = code;
    }
}
