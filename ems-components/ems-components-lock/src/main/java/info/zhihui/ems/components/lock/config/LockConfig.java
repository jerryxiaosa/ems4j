package info.zhihui.ems.components.lock.config;

import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.components.lock.core.impl.local.LocalLock;
import info.zhihui.ems.components.lock.core.impl.local.generator.RwLockGenerator;
import info.zhihui.ems.components.lock.core.impl.local.generator.StdLockGenerator;
import info.zhihui.ems.components.lock.core.impl.redisson.RedissonLock;
import info.zhihui.ems.components.lock.properties.LocalLockProperties;
import info.zhihui.ems.components.lock.properties.LockProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(LockProperties.class)
@Slf4j
public class LockConfig {

    private static final long DEFAULT_LOCAL_LOCK_MAX_SIZE = 1024L;

    @Bean
    @ConditionalOnProperty(name = "lock.distributed", havingValue = "true")
    @ConditionalOnBean(RedissonClient.class)
    public LockTemplate getRedissonLockTemplate(RedissonClient redissonClient) {
        return new RedissonLock(redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean(LockTemplate.class)
    public LockTemplate getLocalLockTemplate(LockProperties lockProperties) {
        LocalLockProperties localLockProperties = lockProperties.getLocalLockProperties();

        long maxSize;
        if (localLockProperties != null && localLockProperties.getMaxSize() > 0) {
            maxSize = localLockProperties.getMaxSize();
        } else {
            log.warn("localLockProperties.maxSize 未配置或非法，使用默认值: {}", DEFAULT_LOCAL_LOCK_MAX_SIZE);
            maxSize = DEFAULT_LOCAL_LOCK_MAX_SIZE;
        }

        StdLockGenerator stdLockGenerator = new StdLockGenerator(maxSize);
        RwLockGenerator rwLockGenerator = new RwLockGenerator(maxSize);

        return new LocalLock(stdLockGenerator, rwLockGenerator);
    }

}
