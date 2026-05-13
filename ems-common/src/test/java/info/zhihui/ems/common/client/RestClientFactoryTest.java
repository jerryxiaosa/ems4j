package info.zhihui.ems.common.client;

import com.sun.net.httpserver.HttpServer;
import info.zhihui.ems.common.factory.RestClientFactory;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientFactoryTest {

    @Test
    void testJsonClient_DefaultAcceptHeader_ContainsApplicationJson() throws IOException {
        AtomicReference<String> acceptHeader = new AtomicReference<>();
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/test", exchange -> {
            acceptHeader.set(exchange.getRequestHeaders().getFirst("Accept"));
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        httpServer.start();

        try {
            RestClient restClient = RestClientFactory.jsonClient(1000, 1000);
            restClient.get()
                    .uri("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/test")
                    .retrieve()
                    .body(String.class);

            assertThat(acceptHeader.get()).contains("application/json");
        } finally {
            httpServer.stop(0);
        }
    }
}
