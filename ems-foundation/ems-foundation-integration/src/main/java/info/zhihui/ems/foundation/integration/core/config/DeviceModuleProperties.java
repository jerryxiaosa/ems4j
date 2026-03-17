package info.zhihui.ems.foundation.integration.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 设备模块配置
 *
 * @author jerryxiaosa
 */
@Data
@ConfigurationProperties(prefix = "device.module")
public class DeviceModuleProperties {

    /**
     * 是否使用真实设备
     */
    private Boolean useRealDevice = true;
}
