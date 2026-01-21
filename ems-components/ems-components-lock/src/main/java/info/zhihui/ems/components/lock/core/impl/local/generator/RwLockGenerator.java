package info.zhihui.ems.components.lock.core.impl.local.generator;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RwLockGenerator extends LockGenerator<ReadWriteLock> {
    public RwLockGenerator(long maxSize) {
        super(maxSize);
    }

    @Override
    ReadWriteLock getNewLock() {
        return new ReentrantReadWriteLock();
    }
}
