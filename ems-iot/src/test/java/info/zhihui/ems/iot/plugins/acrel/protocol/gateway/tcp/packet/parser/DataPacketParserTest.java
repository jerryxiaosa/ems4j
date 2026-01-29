package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayXmlParser;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataPacketParserTest {

    @Test
    void parse_WhenDecryptThrows_ShouldReturnNull() {
        AcrelGatewayCryptoService cryptoService = Mockito.mock(AcrelGatewayCryptoService.class);
        AcrelGatewayXmlParser xmlParser = Mockito.mock(AcrelGatewayXmlParser.class);
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        Device gateway = new Device()
                .setDeviceNo("gw-1")
                .setDeviceSecret("secret");
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(gateway);
        Mockito.when(cryptoService.decrypt(Mockito.any(), Mockito.eq("secret")))
                .thenThrow(new IllegalStateException("decrypt-fail"));
        DataPacketParser parser = new DataPacketParser(cryptoService, xmlParser, deviceResolver);
        ProtocolMessageContext context = buildContext();

        AcrelMessage message = Assertions.assertDoesNotThrow(() -> parser.parse(context, new byte[]{0x01}));

        Assertions.assertNull(message);
        Mockito.verifyNoInteractions(xmlParser);
    }

    private ProtocolMessageContext buildContext() {
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        Mockito.when(session.getSessionId()).thenReturn("session-1");
        return new SimpleProtocolMessageContext().setSession(session);
    }
}
