package info.zhihui.ems.iot.infrastructure.transport.netty.session;

import info.zhihui.ems.iot.protocol.port.ProtocolSessionKey;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

class NettyProtocolSessionTest {

    private static final ProtocolSessionKey<String> TOKEN_KEY = new ProtocolSessionKey<>() {
        @Override
        public String name() {
            return "token";
        }

        @Override
        public Class<String> type() {
            return String.class;
        }
    };

    @Test
    void testGetSessionId_ChannelNull_ReturnsNull() {
        NettyProtocolSession session = new NettyProtocolSession(null);

        Assertions.assertNull(session.getSessionId());
    }

    @Test
    void testGetSessionId_ChannelProvided_ReturnsId() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyProtocolSession session = new NettyProtocolSession(channel);

        Assertions.assertEquals(channel.id().asLongText(), session.getSessionId());
    }

    @Test
    void testIsActive_ChannelNull_ReturnsFalse() {
        NettyProtocolSession session = new NettyProtocolSession(null);

        Assertions.assertFalse(session.isActive());
    }

    @Test
    void testIsActive_ChannelClosed_ReturnsFalse() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyProtocolSession session = new NettyProtocolSession(channel);

        channel.close();

        Assertions.assertFalse(session.isActive());
    }

    @Test
    void testSend_PayloadProvided_WritesToChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyProtocolSession session = new NettyProtocolSession(channel);
        byte[] payload = new byte[]{1, 2, 3};

        session.send(payload);

        Object outbound = channel.readOutbound();
        Assertions.assertNotNull(outbound);
        ByteBuf buffer = (ByteBuf) outbound;
        byte[] actual = new byte[buffer.readableBytes()];
        buffer.readBytes(actual);
        Assertions.assertArrayEquals(payload, actual);
        ReferenceCountUtil.release(buffer);
    }

    @Test
    void testSend_PayloadNull_NoOutboundWrite() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyProtocolSession session = new NettyProtocolSession(channel);

        session.send(null);

        Assertions.assertNull(channel.readOutbound());
    }

    @Test
    void testPublishEvent_EventProvided_FiresUserEvent() {
        AtomicReference<Object> captured = new AtomicReference<>();
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                captured.set(evt);
            }
        });
        NettyProtocolSession session = new NettyProtocolSession(channel);
        Object event = new Object();

        session.publishEvent(event);

        Assertions.assertSame(event, captured.get());
    }

    @Test
    void testPublishEvent_EventNull_NoUserEventTriggered() {
        AtomicReference<Object> captured = new AtomicReference<>();
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                captured.set(evt);
            }
        });
        NettyProtocolSession session = new NettyProtocolSession(channel);

        session.publishEvent(null);

        Assertions.assertNull(captured.get());
    }

    @Test
    void testGetSetRemoveAttribute_DefaultFlow() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyProtocolSession session = new NettyProtocolSession(channel);

        session.setAttribute(TOKEN_KEY, "v1");

        Assertions.assertEquals("v1", session.getAttribute(TOKEN_KEY));

        session.removeAttribute(TOKEN_KEY);

        Assertions.assertNull(session.getAttribute(TOKEN_KEY));
    }

    @Test
    void testGetAttribute_KeyNull_ReturnsNull() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyProtocolSession session = new NettyProtocolSession(channel);

        Assertions.assertNull(session.getAttribute(null));
    }

    @Test
    void testClose_ChannelProvided_ClosesChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyProtocolSession session = new NettyProtocolSession(channel);

        session.close();

        Assertions.assertFalse(channel.isActive());
    }
}
