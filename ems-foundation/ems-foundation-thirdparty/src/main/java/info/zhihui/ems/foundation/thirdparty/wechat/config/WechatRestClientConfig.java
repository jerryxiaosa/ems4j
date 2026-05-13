package info.zhihui.ems.foundation.thirdparty.wechat.config;

import info.zhihui.ems.common.factory.RestClientFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 微信 HTTP 客户端配置。
 */
@Configuration
public class WechatRestClientConfig {

    @Bean
    @Qualifier("wechatRestClient")
    public RestClient wechatRestClient(WechatMiniProgramProperties properties) {
        return RestClientFactory.jsonClient(properties.getConnectTimeoutMillis(), properties.getReadTimeoutMillis());
    }
}
