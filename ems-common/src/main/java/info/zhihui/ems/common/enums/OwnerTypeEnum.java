package info.zhihui.ems.common.enums;

import lombok.Getter;

/**
 * @author jerryxiaosa
 * 账户类型
 */
@Getter
public enum OwnerTypeEnum implements CodeEnum<Integer> {
    ENTERPRISE(0, "企业"),
    PERSONAL(1, "个人"),
    ;

    private final Integer code;

    private final String info;

    OwnerTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
