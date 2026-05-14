package info.zhihui.ems.foundation.thirdparty.wechat.config;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 微信小程序账号配置。
 */
@Data
@Accessors(chain = true)
public class WechatMiniProgramAccountConfig {
    private String appId;
    private String appSecret;
}
