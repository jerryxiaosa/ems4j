package info.zhihui.ems.components.lock.core.impl.local;

import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.components.lock.core.impl.local.generator.RwLockGenerator;
import info.zhihui.ems.components.lock.core.impl.local.generator.StdLockGenerator;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@RequiredArgsConstructor
public class LocalLock implements LockTemplate {
    private final StdLockGenerator stdLockGenerator;
    private final RwLockGenerator rwLockGenerator;

    @Override
    public Lock getLock(String name) {
        return stdLockGenerator.get(name);
    }

    @Override
    public ReadWriteLock getReadWriteLock(String name) {
        return rwLockGenerator.get(name);
    }
}
