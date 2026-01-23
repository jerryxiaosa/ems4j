package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser;

import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayAuthMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayXmlParser;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

class AuthPacketParserTest {

    @Test
    void command_shouldReturnAuth() {
        AcrelGatewayXmlParser xmlParser = Mockito.mock(AcrelGatewayXmlParser.class);
        AuthPacketParser parser = new AuthPacketParser(xmlParser);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH), parser.command());
    }

    @Test
    void parse_whenXmlParserReturnsNull_shouldReturnNull() {
        AcrelGatewayXmlParser xmlParser = Mockito.mock(AcrelGatewayXmlParser.class);
        Mockito.when(xmlParser.parseAuth(Mockito.any())).thenReturn(null);
        AuthPacketParser parser = new AuthPacketParser(xmlParser);
        ProtocolMessageContext context = buildContext();

        GatewayAuthMessage message = (GatewayAuthMessage) parser.parse(context, "x".getBytes(StandardCharsets.UTF_8));

        Assertions.assertNull(message);
    }

    @Test
    void parse_whenXmlParserReturnsMessage_shouldReturnMessage() {
        AcrelGatewayXmlParser xmlParser = Mockito.mock(AcrelGatewayXmlParser.class);
        GatewayAuthMessage expected = new GatewayAuthMessage("gw-1", "b1", "request", null, null);
        Mockito.when(xmlParser.parseAuth(Mockito.any())).thenReturn(expected);
        AuthPacketParser parser = new AuthPacketParser(xmlParser);
        ProtocolMessageContext context = buildContext();

        GatewayAuthMessage message = (GatewayAuthMessage) parser.parse(context, "x".getBytes(StandardCharsets.UTF_8));

        Assertions.assertSame(expected, message);
    }

    private ProtocolMessageContext buildContext() {
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        Mockito.when(session.getSessionId()).thenReturn("session-1");
        return new SimpleProtocolMessageContext().setSession(session);
    }
}
