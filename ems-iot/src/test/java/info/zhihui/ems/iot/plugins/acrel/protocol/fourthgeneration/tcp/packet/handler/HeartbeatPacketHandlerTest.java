package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import info.zhihui.ems.iot.protocol.port.ProtocolInboundPublisher;
import info.zhihui.ems.iot.protocol.port.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.CommonProtocolSessionKeys;
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
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(publisher);

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.HEARTBEAT), handler.command());
    }

    @Test
    void testHandle_DeviceNoMissing_ShouldSkipPublish() {
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(publisher);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(new NettyProtocolSession(channel, new ChannelManager()));

        handler.handle(context, new HeartbeatMessage());

        Mockito.verifyNoInteractions(publisher);
    }

    @Test
    void testHandle_DeviceNoPresent_ShouldPublishEvent() {
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
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
        Mockito.verify(publisher).publish(captor.capture());
        ProtocolHeartbeatInboundEvent event = captor.getValue();
        Assertions.assertEquals("dev-1", event.getDeviceNo());
        Assertions.assertEquals(session.getSessionId(), event.getSessionId());
        Assertions.assertEquals(receivedAt, event.getReceivedAt());
        Assertions.assertEquals(TransportProtocolEnum.TCP, event.getTransportType());
        Assertions.assertEquals(HexUtil.bytesToHexString(context.getRawPayload()), event.getRawPayloadHex());
    }

    @Test
    void testHandle_PublishThrows_ShouldNotPropagateException() {
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        Mockito.doThrow(new IllegalStateException("fail"))
                .when(publisher).publish(Mockito.any());
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(publisher);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel, new ChannelManager());
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "dev-1");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);

        handler.handle(context, new HeartbeatMessage());

        Mockito.verify(publisher).publish(Mockito.any());
    }
}
