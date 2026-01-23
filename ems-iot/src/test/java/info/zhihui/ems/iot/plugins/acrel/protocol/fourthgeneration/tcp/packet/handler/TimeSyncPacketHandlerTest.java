package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.CommonProtocolSessionKeys;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.TimeSyncMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.port.session.DeviceBinder;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TimeSyncPacketHandlerTest {

    @Test
    void command_shouldReturnTimeSync() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        TimeSyncPacketHandler handler = new TimeSyncPacketHandler(deviceBinder, codec);
        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC), handler.command());
    }

    @Test
    void testHandle_MissingSerial_ShouldWriteResponse() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        TimeSyncPacketHandler handler = new TimeSyncPacketHandler(deviceBinder, codec);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = buildSession(channel);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);

        handler.handle(context, new TimeSyncMessage().setSerialNumber(null));

        Assertions.assertNull(session.getAttribute(CommonProtocolSessionKeys.DEVICE_NO));
        Object outbound = channel.readOutbound();
        Assertions.assertNull(outbound);
        Mockito.verifyNoInteractions(deviceBinder);
    }

    @Test
    void testHandle_SerialDifferent_ShouldOverrideDeviceNoAndWrite() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Mockito.doAnswer(invocation -> {
            SimpleProtocolMessageContext ctx = invocation.getArgument(0);
            String deviceNo = invocation.getArgument(1);
            ctx.getSession().setAttribute(CommonProtocolSessionKeys.DEVICE_NO, deviceNo);
            return null;
        }).when(deviceBinder).bind(Mockito.any(), Mockito.anyString());
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        TimeSyncPacketHandler handler = new TimeSyncPacketHandler(deviceBinder, codec);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = buildSession(channel);
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "old");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);

        handler.handle(context, new TimeSyncMessage().setSerialNumber("new"));

        Assertions.assertEquals("new", session.getAttribute(CommonProtocolSessionKeys.DEVICE_NO));
        Object outbound = channel.readOutbound();
        Assertions.assertNotNull(outbound);
        ReferenceCountUtil.release(outbound);
        Mockito.verify(deviceBinder).bind(Mockito.any(), Mockito.eq("new"));
    }

    @Test
    void testHandle_BindThrows_ShouldStillWriteResponse() {
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        Mockito.doThrow(new IllegalStateException("bind-fail"))
                .when(deviceBinder).bind(Mockito.any(), Mockito.anyString());
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        TimeSyncPacketHandler handler = new TimeSyncPacketHandler(deviceBinder, codec);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(buildSession(channel));

        handler.handle(context, new TimeSyncMessage().setSerialNumber("dev-1"));

        Object outbound = channel.readOutbound();
        Assertions.assertNull(outbound);
        Mockito.verify(deviceBinder).bind(Mockito.any(), Mockito.eq("dev-1"));
    }

    private NettyProtocolSession buildSession(EmbeddedChannel channel) {
        ChannelManager manager = new ChannelManager();
        ChannelSession session = new ChannelSession().setChannel(channel);
        manager.register(session);
        return new NettyProtocolSession(channel, manager);
    }
}
