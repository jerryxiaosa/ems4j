package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import org.springframework.context.ApplicationEventPublisher;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.CommonProtocolSessionKeys;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.HeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.util.HexUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;

class HeartbeatPacketHandlerTest {

    @Test
    void command_shouldReturnHeartbeat() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(publisher);

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.HEARTBEAT), handler.command());
    }

    @Test
    void testHandle_DeviceNoMissing_ShouldSkipPublish() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(publisher);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(new NettyProtocolSession(channel, new ChannelManager()));

        handler.handle(context, new HeartbeatMessage());

        Mockito.verifyNoInteractions(publisher);
    }

    @Test
    void testHandle_DeviceNoPresent_ShouldPublishEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(publisher);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel, new ChannelManager());
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "dev-1");
        LocalDateTime receivedAt = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(session)
                .setRawPayload(new byte[]{0x01, 0x02})
                .setReceivedAt(receivedAt)
                .setTransportType(TransportProtocolEnum.TCP);

        handler.handle(context, new HeartbeatMessage());

        ArgumentCaptor<ProtocolHeartbeatInboundEvent> captor = ArgumentCaptor.forClass(ProtocolHeartbeatInboundEvent.class);
        Mockito.verify(publisher).publishEvent(captor.capture());
        ProtocolHeartbeatInboundEvent event = captor.getValue();
        Assertions.assertEquals("dev-1", event.getDeviceNo());
        Assertions.assertEquals(session.getSessionId(), event.getSessionId());
        Assertions.assertEquals(receivedAt, event.getReceivedAt());
        Assertions.assertEquals(TransportProtocolEnum.TCP, event.getTransportType());
        Assertions.assertEquals(HexUtil.bytesToHexString(context.getRawPayload()), event.getRawPayloadHex());
    }

    @Test
    void testHandle_PublishThrows_ShouldNotPropagateException() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        Mockito.doThrow(new IllegalStateException("fail"))
                .when(publisher).publishEvent(Mockito.any(Object.class));
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(publisher);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel, new ChannelManager());
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "dev-1");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(session)
                .setRawPayload(new byte[]{0x01});

        handler.handle(context, new HeartbeatMessage());

        Mockito.verify(publisher).publishEvent(Mockito.any(Object.class));
    }
}
