package info.zhihui.ems.foundation.integration.biz.command.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 设备命令重试配置
 *
 * @author jerryxiaosa
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "device.command.retry")
public class DeviceCommandRetryProperties {

    /**
     * 最大执行次数
     */
    @NotNull
    @Min(1)
    private Integer maxExecuteTimes = 10;

    /**
     * 每次拉取的重试记录数量
     */
    @NotNull
    @Min(1)
    private Integer fetchSize = 100;

    /**
     * 运行态超时时间，单位分钟
     */
    @NotNull
    @Min(1)
    private Integer runningTimeoutMinutes = 10;
}
