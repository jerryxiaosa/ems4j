package info.zhihui.ems.foundation.user.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * 用户第三方身份平台枚举。
 */
@Getter
public enum UserThirdPartyPlatformEnum implements CodeEnum<String> {
    WECHAT_MINI("WECHAT_MINI", "微信小程序"),
    ;

    private final String code;

    private final String info;

    UserThirdPartyPlatformEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
