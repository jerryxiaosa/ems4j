package info.zhihui.ems.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ChannelManager 运行参数。
 * <p>
 * 所有字段都提供默认值，与当前硬编码行为保持一致；
 * 即使未在配置文件中声明，也能按默认参数正常运行。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "iot.channel-manager")
public class ChannelManagerProperties {

    /**
     * 单通道待发送队列上限，防止请求无界堆积带来内存压力。
     */
    private int maxQueueSize = 5;

    /**
     * 等待设备 ACK 的超时时间（毫秒）；超时后会失败当前命令并关闭通道。
     */
    private long commandTimeoutMillis = 15_000L;

    /**
     * 跨线程投递到 EventLoop 后，调用方等待执行结果的最长时间（毫秒）。
     */
    private long eventLoopWaitTimeoutMillis = 3_000L;

    /**
     * 异常统计窗口（毫秒），用于限制短时间异常风暴。
     */
    private long abnormalWindowMillis = 30_000L;

    /**
     * 异常统计阈值：窗口期内超过该次数即判定为异常过载。
     */
    private int abnormalMaxCount = 5;
}
