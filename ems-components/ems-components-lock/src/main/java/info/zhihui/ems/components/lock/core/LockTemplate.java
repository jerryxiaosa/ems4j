package info.zhihui.ems.components.lock.core;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public interface LockTemplate {
    Lock getLock(String name);

    ReadWriteLock getReadWriteLock(String name);
}
