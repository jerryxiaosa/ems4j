package info.zhihui.ems.components.lock.core;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public interface LockTemplate {
    /**
     * 获取普通互斥锁
     * 注意：同一业务 key 请只使用一种锁类型（普通锁或读写锁），不要混用。
     */
    Lock getLock(String name);

    /**
     * 获取读写锁
     * 注意：与同 key 的普通锁不互斥。
     */
    ReadWriteLock getReadWriteLock(String name);
}
