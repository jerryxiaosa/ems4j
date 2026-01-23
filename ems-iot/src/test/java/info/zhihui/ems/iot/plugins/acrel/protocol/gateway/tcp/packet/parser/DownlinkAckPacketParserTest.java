package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayTransparentMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayTransparentCodec;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

class DownlinkAckPacketParserTest {

    @Test
    void command_shouldReturnDownlinkAck() {
        AcrelGatewayCryptoService cryptoService = Mockito.mock(AcrelGatewayCryptoService.class);
        AcrelGatewayTransparentCodec transparentCodec = Mockito.mock(AcrelGatewayTransparentCodec.class);
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        DownlinkAckPacketParser parser = new DownlinkAckPacketParser(cryptoService, transparentCodec, deviceResolver);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK), parser.command());
    }

    @Test
    void parse_whenGatewayMissing_shouldReturnNull() {
        AcrelGatewayCryptoService cryptoService = Mockito.mock(AcrelGatewayCryptoService.class);
        AcrelGatewayTransparentCodec transparentCodec = Mockito.mock(AcrelGatewayTransparentCodec.class);
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(null);
        DownlinkAckPacketParser parser = new DownlinkAckPacketParser(cryptoService, transparentCodec, deviceResolver);
        ProtocolMessageContext context = buildContext();

        GatewayTransparentMessage message = (GatewayTransparentMessage) parser.parse(context, new byte[]{0x01});

        Assertions.assertNull(message);
        Mockito.verifyNoInteractions(cryptoService, transparentCodec);
    }

    @Test
    void parse_whenDecodeReturnsNull_shouldReturnNull() {
        AcrelGatewayCryptoService cryptoService = Mockito.mock(AcrelGatewayCryptoService.class);
        AcrelGatewayTransparentCodec transparentCodec = Mockito.mock(AcrelGatewayTransparentCodec.class);
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        Device gateway = new Device().setDeviceNo("gw-1").setDeviceSecret("secret");
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(gateway);
        Mockito.when(cryptoService.decrypt(Mockito.any(), Mockito.eq("secret")))
                .thenReturn("resp".getBytes(StandardCharsets.UTF_8));
        Mockito.when(transparentCodec.decode(Mockito.any())).thenReturn(null);
        DownlinkAckPacketParser parser = new DownlinkAckPacketParser(cryptoService, transparentCodec, deviceResolver);
        ProtocolMessageContext context = buildContext();

        GatewayTransparentMessage message = (GatewayTransparentMessage) parser.parse(context, new byte[]{0x01});

        Assertions.assertNull(message);
    }

    @Test
    void parse_whenDecodeReturnsMessage_shouldReturnMessage() {
        AcrelGatewayCryptoService cryptoService = Mockito.mock(AcrelGatewayCryptoService.class);
        AcrelGatewayTransparentCodec transparentCodec = Mockito.mock(AcrelGatewayTransparentCodec.class);
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        Device gateway = new Device().setDeviceNo("gw-1").setDeviceSecret("secret");
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(gateway);
        Mockito.when(cryptoService.decrypt(Mockito.any(), Mockito.eq("secret")))
                .thenReturn("resp".getBytes(StandardCharsets.UTF_8));
        GatewayTransparentMessage expected = new GatewayTransparentMessage("meter-1", new byte[]{0x01});
        Mockito.when(transparentCodec.decode(Mockito.any())).thenReturn(expected);
        DownlinkAckPacketParser parser = new DownlinkAckPacketParser(cryptoService, transparentCodec, deviceResolver);
        ProtocolMessageContext context = buildContext();

        GatewayTransparentMessage message = (GatewayTransparentMessage) parser.parse(context, new byte[]{0x01});

        Assertions.assertSame(expected, message);
    }

    private ProtocolMessageContext buildContext() {
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        Mockito.when(session.getSessionId()).thenReturn("session-1");
        return new SimpleProtocolMessageContext().setSession(session);
    }
}
