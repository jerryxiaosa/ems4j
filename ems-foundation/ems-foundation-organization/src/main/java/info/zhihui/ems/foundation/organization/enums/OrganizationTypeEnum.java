package info.zhihui.ems.foundation.organization.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

@Getter
public enum OrganizationTypeEnum implements CodeEnum<Integer> {
    ENTERPRISE(1, "企业");

    private final Integer code;
    private final String info;

    OrganizationTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}