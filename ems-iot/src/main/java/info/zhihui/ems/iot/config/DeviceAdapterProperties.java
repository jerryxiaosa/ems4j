package info.zhihui.ems.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 全局设备适配器配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "transport")
public class DeviceAdapterProperties {

    private NettyProperties netty = new NettyProperties();
    private MqttProperties mqtt = new MqttProperties();

    @Data
    public static class NettyProperties {
        private int port = 19500;
        private int bossThreads = 1;
        private int workerThreads = 4;
        private UnknownProtocolProperties unknownProtocol = new UnknownProtocolProperties();

        /**
         * 业务线程数：用于承载报文业务处理（避免阻塞 Netty IO 线程）。
         * <p>
         * 当值 <= 0 时，将按 {@code max(2 * cpu核心数, workerThreads)} 自动推导。
         */
        private int businessThreads = 0;
    }

    @Data
    public static class UnknownProtocolProperties {
        /**
         * 未识别协议最大累计字节数，<= 0 表示不限制。
         */
        private int maxBytes = 4096;

        /**
         * 未识别协议最大探测次数，<= 0 表示不限制。
         */
        private int maxAttempts = 3;

        /**
         * 未识别协议最大持续时间（毫秒），<= 0 表示不限制。
         */
        private long maxDurationMs = 3000L;
    }

    @Data
    public static class MqttProperties {
        private String url = "tcp://localhost:1883";
        private String clientId = "iot-adapter";
        private String username = "test";
        private String password = "test";
        private String[] topics = new String[]{"sys/#"};
    }
}
