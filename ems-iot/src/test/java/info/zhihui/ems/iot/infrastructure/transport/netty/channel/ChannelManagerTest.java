package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
    void completePending_shouldReturnFalseWhenDeviceUnknown() {
        Assertions.assertFalse(channelManager.completePending("not-exist", new byte[]{1}));
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
    void sendInQueueWithoutWaiting_shouldCompleteOnWrite() {
        channelManager.sendInQueueWithoutWaiting("dev-1", new byte[]{1, 2, 3});
        Assertions.assertNull(session.getPendingFuture());
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
    void completePending_whenNoPending_shouldReturnFalse() {
        ChannelManager manager = new ChannelManager();
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelSession localSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(channel);
        manager.register(localSession);

        Assertions.assertFalse(manager.completePending("dev-1", new byte[]{1}));
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
