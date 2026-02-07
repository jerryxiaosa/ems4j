package info.zhihui.ems.mq.rabbitmq.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 事务消息重试配置
 *
 * @author jerryxiaosa
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "mq.transaction-message.retry")
public class TransactionMessageRetryProperties {

    /**
     * 最大重试次数
     */
    @NotNull
    @Min(1)
    private Integer maxRetryTimes = 10;

    /**
     * 每次拉取重试记录的数量
     */
    @NotNull
    @Min(1)
    private Integer fetchSize = 100;
}
