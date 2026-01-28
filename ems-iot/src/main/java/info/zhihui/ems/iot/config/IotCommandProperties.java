package info.zhihui.ems.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 命令下发相关配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "iot.command")
public class IotCommandProperties {

    /**
     * 默认超时时间（毫秒），用于等待设备响应。
     */
    private long timeoutMillis = 10_000L;
}
