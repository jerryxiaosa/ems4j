package info.zhihui.ems.schedule.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 调度器全局配置
 * 开启 Spring 调度功能
 *
 * @author jerryxiaosa
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig implements SchedulingConfigurer {

    @Value("${schedule.thread-pool-size:4}")
    private int threadPoolSize;

    @Value("${schedule.await-termination-seconds:10}")
    private int awaitTerminationSeconds;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(threadPoolSize);
        scheduler.setThreadNamePrefix("ems-schedule-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(awaitTerminationSeconds);
        scheduler.setErrorHandler(throwable -> log.error("调度任务执行异常", throwable));
        scheduler.initialize();
        return scheduler;
    }
}
