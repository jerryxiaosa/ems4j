package info.zhihui.ems.foundation.thirdparty.config;

import info.zhihui.ems.foundation.thirdparty.wechat.config.WechatMiniProgramProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方平台配置。
 */
@Configuration
@EnableConfigurationProperties({
        WechatMiniProgramProperties.class
})
public class ThirdPartyConfig {
}
