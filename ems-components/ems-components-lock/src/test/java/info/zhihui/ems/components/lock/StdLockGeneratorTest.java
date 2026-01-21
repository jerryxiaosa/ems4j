package info.zhihui.ems.components.lock;

import info.zhihui.ems.components.lock.core.impl.local.generator.StdLockGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class StdLockGeneratorTest {
    @Test
    public void testLock() throws InterruptedException {
        StdLockGenerator stdLockGenerator = new StdLockGenerator(10);
        String key = "test";
        CountDownLatch latch = new CountDownLatch(2);
        LocalDateTime start = LocalDateTime.now();

        // 加锁串行
        new Thread(()-> {
            Lock lock = stdLockGenerator.get(key);
            try {
                lock.lock();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
                lock.unlock();
            }
        }).start();

        new Thread(()-> {
            Lock lock = stdLockGenerator.get(key);
            try {
                lock.lock();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
                lock.unlock();
            }
        }).start();

        latch.await();
        LocalDateTime end = LocalDateTime.now();
        long secondsBetween = ChronoUnit.SECONDS.between(start, end);
        Assertions.assertTrue(secondsBetween >= 1);
    }

    @Test
    public void testLockOverSizeLRU() throws InterruptedException {
        StdLockGenerator stdLockGenerator = new StdLockGenerator(3);
        String key = "test";
        CountDownLatch latch = new CountDownLatch(2);
        LocalDateTime start = LocalDateTime.now();

        new Thread(()-> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            stdLockGenerator.get("4");
            Lock lock = stdLockGenerator.get(key);
            try {
                lock.lock();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
                lock.unlock();
            }
        }).start();

        new Thread(()-> {
            stdLockGenerator.get("1");
            Lock lock = stdLockGenerator.get(key);
            stdLockGenerator.get("3");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                lock.lock();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
                lock.unlock();
            }
        }).start();

        latch.await();
        LocalDateTime end = LocalDateTime.now();
        long secondsBetween = ChronoUnit.SECONDS.between(start, end);
        Assertions.assertTrue(secondsBetween >= 1);
    }
}
