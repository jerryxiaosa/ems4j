package info.zhihui.ems.components.lock.config;

import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.components.lock.core.impl.local.LocalLock;
import info.zhihui.ems.components.lock.core.impl.local.generator.RwLockGenerator;
import info.zhihui.ems.components.lock.core.impl.local.generator.StdLockGenerator;
import info.zhihui.ems.components.lock.core.impl.redisson.RedissonLock;
import info.zhihui.ems.components.lock.properties.LocalLockProperties;
import info.zhihui.ems.components.lock.properties.LockProperties;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(LockProperties.class)
public class LockConfig {
    @Bean
    @ConditionalOnProperty(name = "lock.distributed", havingValue = "true", matchIfMissing = true)
    public LockTemplate getRedissonLockTemplate(RedissonClient redissonClient) {
        return new RedissonLock(redissonClient);
    }

    @Bean
    @ConditionalOnProperty(name = "lock.distributed", havingValue = "false")
    public LockTemplate getLocalLockTemplate (LockProperties lockProperties) {
        LocalLockProperties localLockProperties = lockProperties.getLocalLockProperties();
        StdLockGenerator stdLockGenerator = new StdLockGenerator(localLockProperties.getMaxSize());
        RwLockGenerator rwLockGenerator = new RwLockGenerator(localLockProperties.getMaxSize());

        return new LocalLock(stdLockGenerator, rwLockGenerator);
    }

}
