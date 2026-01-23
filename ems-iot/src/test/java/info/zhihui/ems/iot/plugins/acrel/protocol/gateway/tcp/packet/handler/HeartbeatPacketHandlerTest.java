package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

class HeartbeatPacketHandlerTest {

    @Test
    void command_shouldReturnHeartbeat() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(deviceResolver, deviceRegistry, frameCodec);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT), handler.command());
    }

    @Test
    void handle_whenGatewayMissing_shouldSkip() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(null);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(deviceResolver, deviceRegistry, frameCodec);

        handler.handle(buildContext(), null);

        Mockito.verifyNoInteractions(deviceRegistry, frameCodec);
    }

    @Test
    void handle_whenGatewayPresent_shouldUpdateAndSendAck() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        Device gateway = new Device().setDeviceNo("gw-1");
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(gateway);
        byte[] encoded = new byte[]{0x01};
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.when(frameCodec.encode(Mockito.eq(GatewayPacketCode.HEARTBEAT), payloadCaptor.capture()))
                .thenReturn(encoded);
        HeartbeatPacketHandler handler = new HeartbeatPacketHandler(deviceResolver, deviceRegistry, frameCodec);
        ProtocolMessageContext context = buildContext();

        handler.handle(context, null);

        Mockito.verify(deviceRegistry).update(gateway);
        Assertions.assertNotNull(gateway.getLastOnlineAt());
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

    private ProtocolMessageContext buildContext() {
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        return new SimpleProtocolMessageContext().setSession(session);
    }
}
