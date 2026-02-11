package info.zhihui.ems.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 电量上报推送配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "iot.report.push")
public class IotEnergyReportPushProperties {

    /**
     * 是否启用上报推送。
     */
    private boolean enabled = true;

    /**
     * 能耗系统地址。
     */
    private String baseUrl = "http://127.0.0.1:8080";

    /**
     * 能耗系统上报路径。
     */
    private String path = "/device/energy-reports/standard";

    /**
     * 上报来源标识。
     */
    private String source = "IOT";
}
