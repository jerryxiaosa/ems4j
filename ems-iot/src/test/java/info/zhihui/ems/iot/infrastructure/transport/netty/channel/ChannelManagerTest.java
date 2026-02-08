package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

class ChannelManagerTest {

    private ChannelManager channelManager;
    private EmbeddedChannel channel;
    private ChannelSession session;

    @BeforeEach
    void setUp() {
        channelManager = new ChannelManager();
        channel = new EmbeddedChannel();
        session = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(channel);
        channelManager.register(session);
    }

    @Test
    void sendToDevice_shouldReturnFuture_andCompleteOnWrite() throws Exception {
        CompletableFuture<byte[]> future = channelManager.sendInQueue("dev-1", new byte[]{1, 2, 3});
        channelManager.completePending("dev-1", new byte[]{9});

        future.get(1, TimeUnit.SECONDS); // 无异常即为通过
    }

    @Test
    void sendToDevice_shouldQueueAndDispatchInOrder() throws Exception {
        CompletableFuture<byte[]> first = channelManager.sendInQueue("dev-1", new byte[]{1});
        CompletableFuture<byte[]> second = channelManager.sendInQueue("dev-1", new byte[]{2});

        // 模拟按顺序收到两个响应
        channelManager.completePending("dev-1", new byte[]{9});
        first.get(1, TimeUnit.SECONDS);

        channelManager.completePending("dev-1", new byte[]{8});
        second.get(1, TimeUnit.SECONDS);
    }

    @Test
    void sendToDevice_shouldFailWhenChannelInactive() {
        channel.close();
        Assertions.assertThrows(IllegalStateException.class,
                () -> channelManager.sendInQueue("dev-1", new byte[]{1}));
    }

    @Test
    void register_whenSessionNull_shouldThrow() {
        Assertions.assertThrows(IllegalStateException.class, () -> channelManager.register(null));
    }

    @Test
    void register_whenChannelNull_shouldThrow() {
        ChannelSession invalid = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(null);
        Assertions.assertThrows(IllegalStateException.class, () -> channelManager.register(invalid));
    }

    @Test
    void register_whenDeviceNoBlank_shouldThrow() {
        ChannelSession invalid = new ChannelSession()
                .setDeviceNo(" ")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(new EmbeddedChannel());
        Assertions.assertThrows(IllegalArgumentException.class, () -> channelManager.register(invalid));
    }

    @Test
    void register_whenDeviceNoBlank_shouldNotCorruptExistingMapping() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel sharedChannel = new EmbeddedChannel();
        ChannelSession validSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(sharedChannel);
        manager.register(validSession);

        ChannelSession invalidSession = new ChannelSession()
                .setDeviceNo(" ")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(sharedChannel);
        Assertions.assertThrows(IllegalArgumentException.class, () -> manager.register(invalidSession));

        CompletableFuture<byte[]> future = manager.sendInQueue("dev-1", new byte[]{1});
        Object outbound = sharedChannel.readOutbound();
        ReferenceCountUtil.release(outbound);
        manager.completePending("dev-1", new byte[]{1});
        Assertions.assertArrayEquals(new byte[]{1}, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void remove_whenChannelIdBlank_shouldIgnore() {
        Assertions.assertDoesNotThrow(() -> channelManager.remove(" "));
    }

    @Test
    void closeAndRemove_whenChannelIdBlank_shouldIgnore() {
        Assertions.assertDoesNotThrow(() -> channelManager.closeAndRemove(" "));
    }

    @Test
    void sendInQueue_whenDeviceNoBlank_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> channelManager.sendInQueue(" ", new byte[]{1}));
    }

    @Test
    void queueShouldRejectWhenFull() {
        session.getSending().set(true); // 模拟已有请求在发送中，阻止自动调度
        session.setPendingFuture(new CompletableFuture<>());
        int success = 0;
        CompletableFuture<byte[]> rejected = null;
        for (int i = 0; i < 20; i++) {
            CompletableFuture<byte[]> future = channelManager.sendInQueue("dev-1", new byte[]{(byte) i});
            if (future.isCompletedExceptionally()) {
                rejected = future;
                break;
            }
            success++;
        }
        Assertions.assertNotNull(rejected);
        Assertions.assertEquals(5, success); // 队列容量=5
        Assertions.assertTrue(rejected.isCompletedExceptionally());
    }

    @Test
    void queueShouldKeepLimitWhenConcurrentEnqueue() throws Exception {
        ChannelManager manager = new ChannelManager();
        DefaultEventLoop eventLoop = new DefaultEventLoop();
        try {
            Channel mockChannel = Mockito.mock(Channel.class);
            Mockito.when(mockChannel.id()).thenReturn(DefaultChannelId.newInstance());
            Mockito.when(mockChannel.eventLoop()).thenReturn(eventLoop);
            Mockito.when(mockChannel.isActive()).thenReturn(true);

            ChannelSession localSession = new ChannelSession()
                    .setDeviceNo("dev-1")
                    .setDeviceType(DeviceTypeEnum.ELECTRIC)
                    .setChannel(mockChannel);
            manager.register(localSession);

            localSession.getSending().set(true);
            localSession.setPendingFuture(new CompletableFuture<>());

            int requestCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(requestCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            try {
                List<Future<CompletableFuture<byte[]>>> enqueueTasks = new ArrayList<>();
                for (int i = 0; i < requestCount; i++) {
                    final byte value = (byte) i;
                    enqueueTasks.add(executor.submit(() -> {
                        startLatch.await(1, TimeUnit.SECONDS);
                        return manager.sendInQueue("dev-1", new byte[]{value});
                    }));
                }
                startLatch.countDown();

                int success = 0;
                int rejected = 0;
                for (Future<CompletableFuture<byte[]>> enqueueTask : enqueueTasks) {
                    CompletableFuture<byte[]> resultFuture = enqueueTask.get(1, TimeUnit.SECONDS);
                    if (resultFuture.isCompletedExceptionally()) {
                        rejected++;
                    } else {
                        success++;
                    }
                }
                Assertions.assertEquals(5, success);
                Assertions.assertEquals(requestCount - 5, rejected);
                Assertions.assertEquals(5, localSession.getQueue().size());
            } finally {
                executor.shutdownNow();
            }
        } finally {
            eventLoop.shutdownGracefully().syncUninterruptibly();
        }
    }

    @Test
    void queueShouldKeepLimitWhenConcurrentEnqueue_andMappingPointsToWrongSession() throws Exception {
        ChannelManager manager = new ChannelManager();
        DefaultEventLoop eventLoop = new DefaultEventLoop();
        try {
            Channel firstChannel = Mockito.mock(Channel.class);
            DefaultChannelId firstId = DefaultChannelId.newInstance();
            Mockito.when(firstChannel.id()).thenReturn(firstId);
            Mockito.when(firstChannel.eventLoop()).thenReturn(eventLoop);
            Mockito.when(firstChannel.isActive()).thenReturn(true);

            Channel secondChannel = Mockito.mock(Channel.class);
            DefaultChannelId secondId = DefaultChannelId.newInstance();
            Mockito.when(secondChannel.id()).thenReturn(secondId);
            Mockito.when(secondChannel.eventLoop()).thenReturn(eventLoop);
            Mockito.when(secondChannel.isActive()).thenReturn(true);

            ChannelSession firstSession = new ChannelSession()
                    .setDeviceNo("dev-1")
                    .setDeviceType(DeviceTypeEnum.ELECTRIC)
                    .setChannel(firstChannel);
            ChannelSession secondSession = new ChannelSession()
                    .setDeviceNo("dev-2")
                    .setDeviceType(DeviceTypeEnum.ELECTRIC)
                    .setChannel(secondChannel);
            manager.register(firstSession);
            manager.register(secondSession);

            // 人工制造脏映射：dev-1 被错误指向 dev-2 的通道。
            Field mappingField = ChannelManager.class.getDeclaredField("deviceNoToChannelId");
            mappingField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, String> mapping = (Map<String, String>) mappingField.get(manager);
            mapping.put("dev-1", secondId.asLongText());

            // 锁住 firstSession，确保只验证入队行为，不触发实际发送。
            firstSession.getSending().set(true);
            firstSession.setPendingFuture(new CompletableFuture<>());

            int requestCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(requestCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            try {
                List<Future<CompletableFuture<byte[]>>> enqueueTasks = new ArrayList<>();
                for (int i = 0; i < requestCount; i++) {
                    final byte value = (byte) i;
                    enqueueTasks.add(executor.submit(() -> {
                        startLatch.await(1, TimeUnit.SECONDS);
                        return manager.sendInQueue("dev-1", new byte[]{value});
                    }));
                }
                startLatch.countDown();

                int success = 0;
                int rejected = 0;
                for (Future<CompletableFuture<byte[]>> enqueueTask : enqueueTasks) {
                    CompletableFuture<byte[]> resultFuture = enqueueTask.get(1, TimeUnit.SECONDS);
                    if (resultFuture.isCompletedExceptionally()) {
                        rejected++;
                    } else {
                        success++;
                    }
                }

                Assertions.assertEquals(5, success);
                Assertions.assertEquals(requestCount - 5, rejected);
                Assertions.assertEquals(5, firstSession.getQueue().size());
                Assertions.assertEquals(0, secondSession.getQueue().size());
                Assertions.assertEquals(firstId.asLongText(), mapping.get("dev-1"));
            } finally {
                executor.shutdownNow();
            }
        } finally {
            eventLoop.shutdownGracefully().syncUninterruptibly();
        }
    }

    @Test
    void completePending_shouldResolveFuture() throws Exception {
        CompletableFuture<byte[]> future = channelManager.sendInQueue("dev-1", new byte[]{1, 2});
        boolean completed = channelManager.completePending("dev-1", new byte[]{9, 9});
        Assertions.assertTrue(completed);
        Assertions.assertArrayEquals(new byte[]{9, 9}, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void removeShouldClearQueueAndPending() {
        CompletableFuture<byte[]> future = channelManager.sendInQueue("dev-1", new byte[]{1});
        channelManager.remove(channel.id().asLongText());
        Assertions.assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void sendToDevice_shouldThrowWhenDeviceNotFound() {
        ChannelManager manager = new ChannelManager();
        Assertions.assertThrows(IllegalStateException.class,
                () -> manager.sendInQueue("not-exist", new byte[]{1}));
    }

    @Test
    void completePending_shouldThrowWhenDeviceUnknown() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> channelManager.completePending("not-exist", new byte[]{1}));
    }

    @Test
    void sendToDevice_whenFirstWriteFails_shouldFailFutureAndContinueQueue() throws Exception {
        ChannelManager manager = new ChannelManager();

        AtomicInteger counter = new AtomicInteger();
        EmbeddedChannel failThenSuccess = new EmbeddedChannel(new io.netty.channel.ChannelOutboundHandlerAdapter() {
            @Override
            public void write(io.netty.channel.ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) {
                if (counter.incrementAndGet() == 1) {
                    promise.setFailure(new RuntimeException("fail-once"));
                } else {
                    promise.setSuccess();
                }
            }
        });
        ChannelSession otherSession = new ChannelSession()
                .setDeviceNo("dev-2")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(failThenSuccess);
        manager.register(otherSession);

        CompletableFuture<byte[]> first = manager.sendInQueue("dev-2", new byte[]{1});
        CompletableFuture<byte[]> second = manager.sendInQueue("dev-2", new byte[]{2});

        Assertions.assertThrows(ExecutionException.class, () -> first.get(500, TimeUnit.MILLISECONDS));

        boolean completed = manager.completePending("dev-2", new byte[]{9});
        Assertions.assertTrue(completed);
        Assertions.assertArrayEquals(new byte[]{9}, second.get(1, TimeUnit.SECONDS));
    }

    @Test
    void timeout_shouldCloseChannelAndClearQueue() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel localChannel = new EmbeddedChannel();
        ChannelSession localSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(localChannel);
        manager.register(localSession);

        CompletableFuture<byte[]> first = manager.sendInQueue("dev-1", new byte[]{1});
        CompletableFuture<byte[]> second = manager.sendInQueue("dev-1", new byte[]{2});

        localChannel.advanceTimeBy(16, TimeUnit.SECONDS);
        localChannel.runScheduledPendingTasks();
        localChannel.runPendingTasks();

        Assertions.assertTrue(first.isCompletedExceptionally());
        Assertions.assertTrue(second.isCompletedExceptionally());
        Assertions.assertFalse(localChannel.isActive());
        Assertions.assertThrows(IllegalStateException.class,
                () -> manager.sendInQueue("dev-1", new byte[]{3}));
    }

    @Test
    void sendInQueueWithoutWaiting_shouldCompleteOnWrite() {
        channelManager.sendInQueueWithoutWaiting("dev-1", new byte[]{1, 2, 3});
        Assertions.assertNull(session.getPendingFuture());
    }

    @Test
    void completePending_whenDeviceNoBlank_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> channelManager.completePending(" ", new byte[]{1}));
    }

    @Test
    void register_whenSameChannelDifferentDevice_shouldUpdateMappingAndRemoveOld() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelSession first = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(channel);
        manager.register(first);

        ChannelSession second = new ChannelSession()
                .setDeviceNo("dev-2")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(channel);
        manager.register(second);

        Assertions.assertThrows(IllegalStateException.class,
                () -> manager.sendInQueue("dev-1", new byte[]{1}));

        CompletableFuture<byte[]> future = manager.sendInQueue("dev-2", new byte[]{1});
        Object outbound = channel.readOutbound();
        ReferenceCountUtil.release(outbound);
        manager.completePending("dev-2", new byte[]{1});
        future.get(1, TimeUnit.SECONDS);
    }

    @Test
    void register_whenRebindToNewChannel_shouldCloseOldChannel() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel firstChannel = new EmbeddedChannel(DefaultChannelId.newInstance());
        ChannelSession first = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(firstChannel);
        manager.register(first);

        EmbeddedChannel secondChannel = new EmbeddedChannel(DefaultChannelId.newInstance());
        ChannelSession second = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(secondChannel);
        manager.register(second);

        Assertions.assertFalse(firstChannel.isActive());

        CompletableFuture<byte[]> future = manager.sendInQueue("dev-1", new byte[]{1});
        Object outbound = secondChannel.readOutbound();
        ReferenceCountUtil.release(outbound);
        manager.completePending("dev-1", new byte[]{1});
        future.get(1, TimeUnit.SECONDS);
    }

    @Test
    void register_whenRebindToNewChannel_shouldFailOldPendingAndQueue() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel firstChannel = new EmbeddedChannel(DefaultChannelId.newInstance());
        ChannelSession first = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(firstChannel);
        manager.register(first);

        CompletableFuture<byte[]> pending = manager.sendInQueue("dev-1", new byte[]{1});
        CompletableFuture<byte[]> queued = manager.sendInQueue("dev-1", new byte[]{2});

        EmbeddedChannel secondChannel = new EmbeddedChannel(DefaultChannelId.newInstance());
        ChannelSession second = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(secondChannel);
        manager.register(second);

        Assertions.assertTrue(pending.isCompletedExceptionally());
        Assertions.assertTrue(queued.isCompletedExceptionally());
        Assertions.assertFalse(firstChannel.isActive());

        CompletableFuture<byte[]> activeFuture = manager.sendInQueue("dev-1", new byte[]{3});
        Object outbound = secondChannel.readOutbound();
        ReferenceCountUtil.release(outbound);
        manager.completePending("dev-1", new byte[]{3});
        Assertions.assertArrayEquals(new byte[]{3}, activeFuture.get(1, TimeUnit.SECONDS));
    }

    @Test
    void register_whenConcurrentRebindAcrossChannels_shouldNotHang() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel firstChannel = new EmbeddedChannel(DefaultChannelId.newInstance());
        EmbeddedChannel secondChannel = new EmbeddedChannel(DefaultChannelId.newInstance());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> firstTask = executor.submit(() -> {
                start.await(1, TimeUnit.SECONDS);
                manager.register(new ChannelSession()
                        .setDeviceNo("dev-1")
                        .setDeviceType(DeviceTypeEnum.ELECTRIC)
                        .setChannel(firstChannel));
                return null;
            });
            Future<?> secondTask = executor.submit(() -> {
                start.await(1, TimeUnit.SECONDS);
                manager.register(new ChannelSession()
                        .setDeviceNo("dev-1")
                        .setDeviceType(DeviceTypeEnum.ELECTRIC)
                        .setChannel(secondChannel));
                return null;
            });

            start.countDown();
            firstTask.get(1, TimeUnit.SECONDS);
            secondTask.get(1, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        Assertions.assertTrue(firstChannel.isActive() || secondChannel.isActive());
    }

    @Test
    void remove_shouldCompleteQueuedTasks() {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelSession localSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(channel);
        manager.register(localSession);

        localSession.getSending().set(true);
        List<CompletableFuture<byte[]>> futures = new ArrayList<>();
        futures.add(manager.sendInQueue("dev-1", new byte[]{1}));
        futures.add(manager.sendInQueue("dev-1", new byte[]{2}));

        manager.remove(channel.id().asLongText());

        for (CompletableFuture<byte[]> future : futures) {
            Assertions.assertTrue(future.isCompletedExceptionally());
        }
    }

    @Test
    void completePending_whenNoPending_shouldThrow() {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelSession localSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(channel);
        manager.register(localSession);

        Assertions.assertThrows(IllegalStateException.class,
                () -> manager.completePending("dev-1", new byte[]{1}));
    }

    @Test
    void completePending_whenLateAckAfterTimeout_shouldThrow() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel localChannel = new EmbeddedChannel();
        ChannelSession localSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(localChannel);
        manager.register(localSession);

        CompletableFuture<byte[]> future = manager.sendInQueue("dev-1", new byte[]{1});
        localChannel.advanceTimeBy(16, TimeUnit.SECONDS);
        localChannel.runScheduledPendingTasks();
        localChannel.runPendingTasks();

        ExecutionException exception = Assertions.assertThrows(ExecutionException.class,
                () -> future.get(1, TimeUnit.SECONDS));
        Assertions.assertInstanceOf(TimeoutException.class, exception.getCause());
        Assertions.assertThrows(IllegalStateException.class,
                () -> manager.completePending("dev-1", new byte[]{9}));
    }

    @Test
    void timeout_shouldIgnoreStaleTaskAndKeepNewPending() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel localChannel = new EmbeddedChannel();
        ChannelSession localSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(localChannel);
        manager.register(localSession);

        CompletableFuture<byte[]> first = manager.sendInQueue("dev-1", new byte[]{1});
        manager.completePending("dev-1", new byte[]{1});
        Assertions.assertArrayEquals(new byte[]{1}, first.get(1, TimeUnit.SECONDS));

        localChannel.advanceTimeBy(1, TimeUnit.SECONDS);
        CompletableFuture<byte[]> second = manager.sendInQueue("dev-1", new byte[]{2});

        localChannel.advanceTimeBy(9, TimeUnit.SECONDS);
        localChannel.runScheduledPendingTasks();
        localChannel.runPendingTasks();

        Assertions.assertFalse(second.isCompletedExceptionally());
        manager.completePending("dev-1", new byte[]{2});
        Assertions.assertArrayEquals(new byte[]{2}, second.get(1, TimeUnit.SECONDS));
    }

    @Test
    void sendWithAck_whenMappingStale_shouldRecoverByScan() throws Exception {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelSession localSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(channel);
        manager.register(localSession);

        Field mappingField = ChannelManager.class.getDeclaredField("deviceNoToChannelId");
        mappingField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) mappingField.get(manager);
        mapping.put("dev-1", "missing-channel");

        CompletableFuture<byte[]> future = manager.sendInQueue("dev-1", new byte[]{1});
        Object outbound = channel.readOutbound();
        ReferenceCountUtil.release(outbound);
        manager.completePending("dev-1", new byte[]{1});
        future.get(1, TimeUnit.SECONDS);
    }

    @Test
    void recordAbnormal_shouldReturnFalseWhenChannelMissing() {
        ChannelManager manager = new ChannelManager();
        boolean exceeded = manager.recordAbnormal("missing", AbnormalReasonEnum.CRC_INVALID, 1L);
        Assertions.assertFalse(exceeded);
    }

    @Test
    void recordAbnormal_shouldExceedWhenCountBeyondThreshold() {
        String channelId = channel.id().asLongText();
        for (int i = 0; i < 5; i++) {
            boolean exceeded = channelManager.recordAbnormal(channelId, AbnormalReasonEnum.CRC_INVALID, 1000L);
            Assertions.assertFalse(exceeded);
        }
        boolean exceeded = channelManager.recordAbnormal(channelId, AbnormalReasonEnum.CRC_INVALID, 1000L);
        Assertions.assertTrue(exceeded);
    }

    @Test
    void recordAbnormal_shouldDropOldEntriesBeyondWindow() {
        String channelId = channel.id().asLongText();
        channelManager.recordAbnormal(channelId, AbnormalReasonEnum.CRC_INVALID, 0L);
        channelManager.recordAbnormal(channelId, AbnormalReasonEnum.CRC_INVALID, 1L);
        channelManager.recordAbnormal(channelId, AbnormalReasonEnum.CRC_INVALID, 31_000L);
        Assertions.assertEquals(1, session.getAbnormalTimestamps().size());
    }

}
