package info.zhihui.ems.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 在线状态相关配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "iot.online")
public class IotOnlineProperties {

    /**
     * 在线判定阈值（秒）。
     */
    private long timeoutSeconds = 300L;
}
