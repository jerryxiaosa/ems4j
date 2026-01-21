package info.zhihui.ems.components.context;


import info.zhihui.ems.common.utils.ThreadLocalUtil;
import info.zhihui.ems.components.context.constant.RequestContextConstant;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class RequestContextTest {
    @InjectMocks
    private RequestContext requestContext;

    @Test
    public void testNull() {
        ThreadLocalUtil.put(RequestContextConstant.USER_REQUEST_DATA, null);

        Assertions.assertNull(requestContext.getUserPhone());
        Assertions.assertNull(requestContext.getUserRealName());
    }

    @Test
    public void testCompletableFuture() {
        RequestContextSetter.doSet( 5);

        log.info("main: {}", requestContext.getUserId());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<CompletableFuture<Void>> futureAll = new ArrayList<>();
        int all = 5;

        // 校验使用主线程的设置
        RequestContextSetter.doSet(3);
        UserRequestData userRequestData = new UserRequestData("张三",  "1388888888");
        ThreadLocalUtil.put(RequestContextConstant.USER_REQUEST_DATA, userRequestData);
        for (int i = 0; i < all; i++) {
            futureAll.add(
                    CompletableFuture.runAsync(() -> {
                        log.info("user id: {}", requestContext.getUserId());
                        log.info("user phone: {}", requestContext.getUserPhone());
                        log.info("user real name: {}", requestContext.getUserRealName());

                        Assertions.assertEquals(3, requestContext.getUserId());
                        Assertions.assertEquals("1388888888", requestContext.getUserPhone());
                        Assertions.assertEquals("张三", requestContext.getUserRealName());
                    }, executor)
            );
        }

        CompletableFuture.allOf(futureAll.toArray(new CompletableFuture[0])).join();

        RequestContextSetter.doSet(4);
        UserRequestData userRequestData2 = new UserRequestData("张三2",  "13999999");
        ThreadLocalUtil.put(RequestContextConstant.USER_REQUEST_DATA, userRequestData2);

        for (int i = 0; i < all; i++) {
            futureAll.add(
                    CompletableFuture.runAsync(() -> {
                        log.info("user id: {}", requestContext.getUserId());
                        log.info("user phone: {}", requestContext.getUserPhone());
                        log.info("user real name: {}", requestContext.getUserRealName());

                        Assertions.assertEquals(4, requestContext.getUserId());
                        Assertions.assertEquals("13999999", requestContext.getUserPhone());
                        Assertions.assertEquals("张三2", requestContext.getUserRealName());
                    }, executor)
            );
        }
    }

    @Test
    public void testNewThread() {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        new Thread(() -> {
            try {
                RequestContextSetter.doSet(1);
                Assertions.assertEquals(1, requestContext.getUserId());
                log.info("[thread1] userId: {}", requestContext.getUserId());

            } catch (Exception e) {
                log.error("error: ", e);
            } finally {
                countDownLatch.countDown();
            }
        }).start();

        new Thread(() -> {
            // 后执行
            try {
                Thread.sleep(500);
                Assertions.assertNull(requestContext.getUserId());
                RequestContextSetter.doSet(2);
                Assertions.assertEquals(2, requestContext.getUserId());
                log.info("[thread2] userId: {}", requestContext.getUserId());
            } catch (Exception e) {
                log.error("error: ", e);
            } finally {
                countDownLatch.countDown();
            }
        }).start();

        try {
            countDownLatch.await();
            log.info("finish");
        } catch (Exception e) {
            log.error("error: ", e);
        }
    }

}

