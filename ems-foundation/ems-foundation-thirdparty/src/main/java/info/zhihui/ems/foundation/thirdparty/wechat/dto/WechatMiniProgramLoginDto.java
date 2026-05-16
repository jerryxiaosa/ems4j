package info.zhihui.ems.foundation.thirdparty.wechat.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 微信小程序登录身份信息。
 */
@Data
@Accessors(chain = true)
public class WechatMiniProgramLoginDto {
    private String appId;
    private String openId;
    private String unionId;
    private String sessionKey;
    private String purePhoneNumber;
}
