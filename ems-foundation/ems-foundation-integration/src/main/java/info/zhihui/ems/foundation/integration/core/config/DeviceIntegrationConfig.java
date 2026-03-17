package info.zhihui.ems.foundation.integration.core.config;

import info.zhihui.ems.foundation.integration.biz.command.config.DeviceCommandRetryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 设备集成配置
 *
 * @author jerryxiaosa
 */
@Configuration
@EnableConfigurationProperties({
        DeviceCommandRetryProperties.class,
        DeviceModuleProperties.class
})
public class DeviceIntegrationConfig {
}
