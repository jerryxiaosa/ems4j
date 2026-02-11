package info.zhihui.ems.iot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * IoT 模块异步执行配置。
 */
@Configuration
public class IotAsyncConfig {

    /**
     * 能耗上报推送线程池核心线程数。
     */
    private static final int ENERGY_REPORT_PUSH_CORE_POOL_SIZE = 2;

    /**
     * 能耗上报推送线程池最大线程数。
     */
    private static final int ENERGY_REPORT_PUSH_MAX_POOL_SIZE = 8;

    /**
     * 能耗上报推送线程池队列容量。
     */
    private static final int ENERGY_REPORT_PUSH_QUEUE_CAPACITY = 2000;

    /**
     * 能耗上报推送线程池线程空闲保活时间（秒）。
     */
    private static final int ENERGY_REPORT_PUSH_KEEP_ALIVE_SECONDS = 60;

    /**
     * 能耗上报推送线程池线程名前缀。
     */
    private static final String ENERGY_REPORT_PUSH_THREAD_NAME_PREFIX = "iot-report-push-";

    /**
     * 能耗上报推送异步线程池。
     */
    @Bean("iotEnergyReportPushExecutor")
    public ThreadPoolTaskExecutor iotEnergyReportPushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ENERGY_REPORT_PUSH_CORE_POOL_SIZE);
        executor.setMaxPoolSize(ENERGY_REPORT_PUSH_MAX_POOL_SIZE);
        executor.setQueueCapacity(ENERGY_REPORT_PUSH_QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(ENERGY_REPORT_PUSH_KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(ENERGY_REPORT_PUSH_THREAD_NAME_PREFIX);
        // 队列满时直接拒绝，避免回退到协议接入线程执行。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
