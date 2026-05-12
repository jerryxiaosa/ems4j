package info.zhihui.ems.foundation.thirdparty.wechat.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信小程序配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "third-party.wechat.mini-program")
public class WechatMiniProgramProperties {

    private String appId;

    private String appSecret;

    @NotNull
    private Integer connectTimeoutMillis = 5000;

    @NotNull
    private Integer readTimeoutMillis = 10000;

    @NotNull
    private Integer accessTokenExpireAheadSeconds = 300;
}
