package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayTransparentMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DownlinkAckPacketHandlerTest {

    @Test
    void command_shouldReturnDownlinkAck() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DownlinkAckPacketHandler handler = new DownlinkAckPacketHandler(deviceResolver, commandTransport);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK), handler.command());
    }

    @Test
    void handle_whenPayloadNull_shouldSkip() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DownlinkAckPacketHandler handler = new DownlinkAckPacketHandler(deviceResolver, commandTransport);

        handler.handle(buildContext(), null);

        Mockito.verifyNoInteractions(deviceResolver, commandTransport);
    }

    @Test
    void handle_whenGatewayMissing_shouldSkipComplete() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(null);
        DownlinkAckPacketHandler handler = new DownlinkAckPacketHandler(deviceResolver, commandTransport);
        GatewayTransparentMessage message = new GatewayTransparentMessage("meter-1", new byte[]{0x01});

        handler.handle(buildContext(), message);

        Mockito.verifyNoInteractions(commandTransport);
    }

    @Test
    void handle_whenGatewayPresent_shouldCompletePending() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        Device gateway = new Device().setDeviceNo("gw-1");
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(gateway);
        DownlinkAckPacketHandler handler = new DownlinkAckPacketHandler(deviceResolver, commandTransport);
        byte[] payload = new byte[]{0x01, 0x02};
        GatewayTransparentMessage message = new GatewayTransparentMessage("meter-1", payload);

        handler.handle(buildContext(), message);

        Mockito.verify(commandTransport).completePending("gw-1", payload);
    }

    private ProtocolMessageContext buildContext() {
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        return new SimpleProtocolMessageContext().setSession(session);
    }
}
