package info.zhihui.ems.iot.config;

import info.zhihui.ems.common.factory.RestClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * IoT 模块 HTTP 客户端配置。
 */
@Configuration
public class IotRestClientConfig {

    /**
     * IoT HTTP 客户端连接超时时间（毫秒）。
     */
    private static final int IOT_HTTP_CONNECT_TIMEOUT_MS = 5000;

    /**
     * IoT HTTP 客户端读取超时时间（毫秒）。
     */
    private static final int IOT_HTTP_READ_TIMEOUT_MS = 10000;

    /**
     * IoT 模块统一使用的 RestClient。
     */
    @Bean("iotRestClient")
    public RestClient iotRestClient() {
        return RestClientFactory.jsonClient(IOT_HTTP_CONNECT_TIMEOUT_MS, IOT_HTTP_READ_TIMEOUT_MS);
    }
}
