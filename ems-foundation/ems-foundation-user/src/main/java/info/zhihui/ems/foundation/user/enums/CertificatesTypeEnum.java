package info.zhihui.ems.foundation.user.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * 证件类型枚举（使用字符串code以兼容当前实体字段）
 */
@Getter
public enum CertificatesTypeEnum implements CodeEnum<Integer> {
    ID_CARD(1),
    PASSPORT(2),
    DRIVER_LICENSE(3),
    RESIDENCE_PERMIT(4),
    OTHER(100),
    ;

    private final Integer code;

    CertificatesTypeEnum(Integer code) {
        this.code = code;
    }
}