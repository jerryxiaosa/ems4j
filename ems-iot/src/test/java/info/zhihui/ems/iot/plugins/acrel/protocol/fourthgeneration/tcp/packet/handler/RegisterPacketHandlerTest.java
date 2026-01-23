package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.DeviceBinder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RegisterPacketHandlerTest {

    @Test
    void command_shouldReturnRegister() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        RegisterPacketHandler handler = new RegisterPacketHandler(deviceBinder, codec);
        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER), handler.command());
    }

    @Test
    void handle_whenSerialMissing_shouldForceClose() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        RegisterPacketHandler handler = new RegisterPacketHandler(deviceBinder, codec);
        EventCaptureHandler capture = new EventCaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(capture);

        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(buildSession(channel));
        RegisterMessage message = new RegisterMessage().setSerialNumber("");

        handler.handle(context, message);

        Assertions.assertNotNull(capture.event);
        Assertions.assertEquals(AbnormalReasonEnum.ILLEGAL_DEVICE, capture.event.reason());
        Assertions.assertTrue(capture.event.forceClose());
        Mockito.verifyNoInteractions(deviceBinder);
    }

    @Test
    void handle_whenBindSuccess_shouldWriteAck() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        RegisterPacketHandler handler = new RegisterPacketHandler(deviceBinder, codec);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(buildSession(channel));
        RegisterMessage message = new RegisterMessage().setSerialNumber("dev-1");

        handler.handle(context, message);

        Object outbound = channel.readOutbound();
        Assertions.assertNotNull(outbound);
        byte[] expected = codec.encodeAck(Acrel4gPacketCode.REGISTER);
        byte[] actual = new byte[((io.netty.buffer.ByteBuf) outbound).readableBytes()];
        ((io.netty.buffer.ByteBuf) outbound).readBytes(actual);
        ReferenceCountUtil.release(outbound);
        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    void handle_whenDeviceNotFound_shouldForceClose() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        RegisterPacketHandler handler = new RegisterPacketHandler(deviceBinder, codec);
        EventCaptureHandler capture = new EventCaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(capture);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(buildSession(channel));
        RegisterMessage message = new RegisterMessage().setSerialNumber("dev-1");
        Mockito.doThrow(new NotFoundException("not found")).when(deviceBinder).bind(context, "dev-1");

        handler.handle(context, message);

        Assertions.assertNotNull(capture.event);
        Assertions.assertEquals(AbnormalReasonEnum.ILLEGAL_DEVICE, capture.event.reason());
        Assertions.assertTrue(capture.event.forceClose());
        Assertions.assertNull(channel.readOutbound());
    }

    @Test
    void handle_whenBindThrows_shouldForceClose() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        RegisterPacketHandler handler = new RegisterPacketHandler(deviceBinder, codec);
        EventCaptureHandler capture = new EventCaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(capture);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(buildSession(channel));
        RegisterMessage message = new RegisterMessage().setSerialNumber("dev-1");
        Mockito.doThrow(new IllegalStateException("fail")).when(deviceBinder).bind(context, "dev-1");

        handler.handle(context, message);

        Assertions.assertNotNull(capture.event);
        Assertions.assertEquals(AbnormalReasonEnum.BUSINESS_ERROR, capture.event.reason());
        Assertions.assertTrue(capture.event.forceClose());
        Assertions.assertNull(channel.readOutbound());
    }

    private NettyProtocolSession buildSession(EmbeddedChannel channel) {
        ChannelManager manager = new ChannelManager();
        ChannelSession session = new ChannelSession().setChannel(channel);
        manager.register(session);
        return new NettyProtocolSession(channel, manager);
    }

    private static class EventCaptureHandler extends ChannelInboundHandlerAdapter {

        private AbnormalEvent event;

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof AbnormalEvent abnormalEvent) {
                event = abnormalEvent;
            }
        }
    }
}
