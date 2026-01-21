package info.zhihui.ems.components.lock.core.impl.redisson;

import info.zhihui.ems.components.lock.core.LockTemplate;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@RequiredArgsConstructor
public class RedissonLock implements LockTemplate {
    private final RedissonClient redissonClient;

    @Override
    public Lock getLock(String name) {
        return redissonClient.getLock(name);
    }

    @Override
    public ReadWriteLock getReadWriteLock(String name) {
        return redissonClient.getReadWriteLock(name);
    }
}
