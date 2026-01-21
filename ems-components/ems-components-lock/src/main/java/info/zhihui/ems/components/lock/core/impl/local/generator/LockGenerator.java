package info.zhihui.ems.components.lock.core.impl.local.generator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


@Slf4j
public abstract class LockGenerator<T> {
    private final Cache<String, T> lruContainer;

    public LockGenerator(long maxSize) {
        long DEFAULT_LOCKING_SECONDS = 60 * 60;

        this.lruContainer = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(DEFAULT_LOCKING_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    abstract T getNewLock();

    @SneakyThrows
    // @NOTICE 线程安全的实现
    public T get(String key) {
        return lruContainer.get(key, () -> {
            log.debug("get new lock: {}", key);
            return getNewLock();
        });
    }
}
