package info.zhihui.ems.components.lock.core.impl.local.generator;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StdLockGenerator extends LockGenerator<Lock> {
    public StdLockGenerator(long maxSize) {
        super(maxSize);
    }

    @Override
    Lock getNewLock() {
        return new ReentrantLock();
    }
}
