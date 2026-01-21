package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbnormalEventHandlerTest {

    @Test
    void userEventTriggered_whenNotAbnormalEvent_shouldPropagate() {
        StubChannelManager manager = new StubChannelManager(false);
        AbnormalEventHandler handler = new AbnormalEventHandler(manager);
        CaptureHandler capture = new CaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler, capture);

        Object event = "test-event";
        channel.pipeline().fireUserEventTriggered(event);

        Assertions.assertSame(event, capture.event);
        Assertions.assertFalse(manager.recordCalled);
    }

    @Test
    void userEventTriggered_whenNotExceeded_shouldPropagate() {
        StubChannelManager manager = new StubChannelManager(false);
        AbnormalEventHandler handler = new AbnormalEventHandler(manager);
        CaptureHandler capture = new CaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler, capture);

        AbnormalEvent event = new AbnormalEvent(AbnormalReasonEnum.CRC_INVALID, 1L, "crc");
        channel.pipeline().fireUserEventTriggered(event);

        Assertions.assertSame(event, capture.event);
        Assertions.assertTrue(manager.recordCalled);
        Assertions.assertNull(manager.removedChannelId);
        Assertions.assertTrue(channel.isActive());
    }

    @Test
    void userEventTriggered_whenExceeded_shouldCloseAndRemove() {
        StubChannelManager manager = new StubChannelManager(true);
        AbnormalEventHandler handler = new AbnormalEventHandler(manager);
        CaptureHandler capture = new CaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler, capture);

        AbnormalEvent event = new AbnormalEvent(AbnormalReasonEnum.CRC_INVALID, 2L, "crc");
        channel.pipeline().fireUserEventTriggered(event);

        Assertions.assertFalse(channel.isActive());
        Assertions.assertEquals(channel.id().asLongText(), manager.removedChannelId);
        Assertions.assertNull(capture.event);
    }

    @Test
    void userEventTriggered_whenForceClose_shouldCloseAndRemove() {
        StubChannelManager manager = new StubChannelManager(false);
        AbnormalEventHandler handler = new AbnormalEventHandler(manager);
        CaptureHandler capture = new CaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler, capture);

        AbnormalEvent event = new AbnormalEvent(AbnormalReasonEnum.BUSINESS_ERROR, 3L, "force", true);
        channel.pipeline().fireUserEventTriggered(event);

        Assertions.assertFalse(channel.isActive());
        Assertions.assertEquals(channel.id().asLongText(), manager.removedChannelId);
        Assertions.assertNull(capture.event);
    }

    private static class StubChannelManager extends ChannelManager {

        private final boolean exceeded;
        private boolean recordCalled;
        private String removedChannelId;

        private StubChannelManager(boolean exceeded) {
            this.exceeded = exceeded;
        }

        @Override
        public boolean recordAbnormal(String channelId, AbnormalReasonEnum reason, long nowMillis) {
            recordCalled = true;
            return exceeded;
        }

        @Override
        public void remove(String channelId) {
            removedChannelId = channelId;
        }
    }

    private static class CaptureHandler extends ChannelInboundHandlerAdapter {

        private Object event;

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            event = evt;
        }
    }
}
