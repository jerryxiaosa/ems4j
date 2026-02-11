package info.zhihui.ems.iot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * IoT 模块 HTTP 客户端配置。
 */
@Configuration
public class IotRestClientConfig {

    /**
     * IoT HTTP 客户端连接超时时间（毫秒）。
     */
    private static final int IOT_HTTP_CONNECT_TIMEOUT_MS = 1000;

    /**
     * IoT HTTP 客户端读取超时时间（毫秒）。
     */
    private static final int IOT_HTTP_READ_TIMEOUT_MS = 3000;

    /**
     * IoT 模块统一使用的 RestClient。
     */
    @Bean("iotRestClient")
    public RestClient iotRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(IOT_HTTP_CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(IOT_HTTP_READ_TIMEOUT_MS);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
