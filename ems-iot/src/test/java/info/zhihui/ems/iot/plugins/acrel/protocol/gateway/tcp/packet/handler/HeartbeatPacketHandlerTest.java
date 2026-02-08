package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import org.springframework.context.ApplicationEventPublisher;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

class HeartbeatPacketHandlerTest {

    @Test
    void command_shouldReturnHeartbeat() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        ApplicationEventPublisher protocolInboundPublisher = Mockito.mock(ApplicationEventPublisher.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(deviceResolver, protocolInboundPublisher, frameCodec);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT), handler.command());
    }

    @Test
    void handle_whenGatewayMissing_shouldSkip() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        ApplicationEventPublisher protocolInboundPublisher = Mockito.mock(ApplicationEventPublisher.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(null);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(deviceResolver, protocolInboundPublisher, frameCodec);

        handler.handle(buildContext(), null);

        Mockito.verifyNoInteractions(protocolInboundPublisher, frameCodec);
    }

    @Test
    void handle_whenGatewayPresent_shouldPublishAndSendAck() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        ApplicationEventPublisher protocolInboundPublisher = Mockito.mock(ApplicationEventPublisher.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        Device gateway = new Device().setDeviceNo("gw-1");
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(gateway);
        byte[] encoded = new byte[]{0x01};
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.when(frameCodec.encode(Mockito.eq(GatewayPacketCode.HEARTBEAT), payloadCaptor.capture()))
                .thenReturn(encoded);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(deviceResolver, protocolInboundPublisher, frameCodec);
        SimpleProtocolMessageContext context = buildContext();
        context.setReceivedAt(LocalDateTime.of(2024, 1, 2, 3, 4, 5))
                .setTransportType(TransportProtocolEnum.TCP)
                .setRawPayload(new byte[]{0x01, 0x02});

        handler.handle(context, null);

        ArgumentCaptor<ProtocolHeartbeatInboundEvent> eventCaptor =
                ArgumentCaptor.forClass(ProtocolHeartbeatInboundEvent.class);
        Mockito.verify(protocolInboundPublisher).publishEvent(eventCaptor.capture());
        ProtocolHeartbeatInboundEvent event = eventCaptor.getValue();
        Assertions.assertEquals("gw-1", event.getDeviceNo());
        Assertions.assertEquals("session-1", event.getSessionId());
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 2, 3, 4, 5), event.getReceivedAt());
        Assertions.assertEquals(TransportProtocolEnum.TCP, event.getTransportType());
        Assertions.assertEquals("0102", event.getRawPayloadHex());
        Mockito.verify(frameCodec).encode(Mockito.eq(GatewayPacketCode.HEARTBEAT), Mockito.any());
        Mockito.verify(context.getSession()).send(encoded);
        byte[] payload = payloadCaptor.getValue();
        String xml = new String(payload, StandardCharsets.UTF_8);
        Assertions.assertTrue(xml.startsWith(
                "<?xml version=\"1.0\"?><root><common><building_id></building_id><gateway_id>gw-1</gateway_id>" +
                        "<type>heart_beat</type></common><heart_beat operation=\"time\"><time>"));
        Assertions.assertTrue(xml.endsWith("</time></heart_beat></root>"));
        int start = xml.indexOf("<time>");
        int end = xml.indexOf("</time>");
        Assertions.assertTrue(start > 0);
        Assertions.assertTrue(end > start);
        String time = xml.substring(start + 6, end);
        Assertions.assertTrue(time.matches("\\d{14}"));
    }

    private SimpleProtocolMessageContext buildContext() {
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        Mockito.when(session.getSessionId()).thenReturn("session-1");
        return new SimpleProtocolMessageContext().setSession(session);
    }
}
