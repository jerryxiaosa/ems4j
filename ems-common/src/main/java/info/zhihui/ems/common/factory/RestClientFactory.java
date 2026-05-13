package info.zhihui.ems.common.factory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient 工厂。
 */
public final class RestClientFactory {

    private RestClientFactory() {
    }

    /**
     * 创建默认接收 JSON 响应的 RestClient。
     *
     * @param connectTimeoutMillis 连接超时时间（毫秒）
     * @param readTimeoutMillis    读取超时时间（毫秒）
     * @return RestClient
     */
    public static RestClient jsonClient(int connectTimeoutMillis, int readTimeoutMillis) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMillis);
        requestFactory.setReadTimeout(readTimeoutMillis);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
