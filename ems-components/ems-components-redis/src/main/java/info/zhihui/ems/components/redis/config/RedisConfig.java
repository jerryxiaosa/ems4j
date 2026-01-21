package info.zhihui.ems.components.redis.config;

import info.zhihui.ems.components.redis.properties.RedissonProperties;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(RedissonProperties.class)
public class RedisConfig {

    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer(RedissonProperties redissonProperties) {
        return new RedissonCustomizer(redissonProperties);
    }
}
