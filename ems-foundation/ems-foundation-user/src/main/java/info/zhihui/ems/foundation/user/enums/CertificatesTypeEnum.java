package info.zhihui.ems.foundation.user.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * 证件类型枚举
 */
@Getter
public enum CertificatesTypeEnum implements CodeEnum<Integer> {
    ID_CARD(1, "身份证"),
    PASSPORT(2, "护照"),
    DRIVER_LICENSE(3, "驾驶证"),
    RESIDENCE_PERMIT(4, "居住证"),
    OTHER(100, "其他"),
    ;

    private final Integer code;
    private final String info;

    CertificatesTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
