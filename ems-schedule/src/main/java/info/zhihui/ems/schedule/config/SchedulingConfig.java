package info.zhihui.ems.schedule.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 调度器全局配置
 * 开启 Spring 调度功能
 *
 * @author jerryxiaosa
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 如需线程池或时区配置，可在此扩展
}