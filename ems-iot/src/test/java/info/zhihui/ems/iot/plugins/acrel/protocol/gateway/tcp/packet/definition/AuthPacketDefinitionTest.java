package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler.AuthPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser.AuthPacketParser;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuthPacketDefinitionTest {

    @Test
    void command_shouldReturnAuth() {
        AuthPacketParser parser = Mockito.mock(AuthPacketParser.class);
        AuthPacketHandler handler = Mockito.mock(AuthPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH));
        AuthPacketDefinition definition = new AuthPacketDefinition(parser, handler);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH), definition.command());
    }

    @Test
    void parse_shouldDelegateToParser() {
        AuthPacketParser parser = Mockito.mock(AuthPacketParser.class);
        AuthPacketHandler handler = Mockito.mock(AuthPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH));
        AuthPacketDefinition definition = new AuthPacketDefinition(parser, handler);
        ProtocolMessageContext context = Mockito.mock(ProtocolMessageContext.class);
        byte[] payload = new byte[]{0x01};
        AcrelMessage message = Mockito.mock(AcrelMessage.class);
        Mockito.when(parser.parse(context, payload)).thenReturn(message);

        AcrelMessage result = definition.parse(context, payload);

        Assertions.assertSame(message, result);
        Mockito.verify(parser).parse(context, payload);
    }

    @Test
    void handle_shouldDelegateToHandler() {
        AuthPacketParser parser = Mockito.mock(AuthPacketParser.class);
        AuthPacketHandler handler = Mockito.mock(AuthPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH));
        AuthPacketDefinition definition = new AuthPacketDefinition(parser, handler);
        ProtocolMessageContext context = Mockito.mock(ProtocolMessageContext.class);
        AcrelMessage message = Mockito.mock(AcrelMessage.class);

        definition.handle(context, message);

        Mockito.verify(handler).handle(context, message);
    }

    @Test
    void constructor_whenCommandMismatch_shouldThrow() {
        AuthPacketParser parser = Mockito.mock(AuthPacketParser.class);
        AuthPacketHandler handler = Mockito.mock(AuthPacketHandler.class);
        Mockito.when(parser.command()).thenReturn("ff");
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH));

        Assertions.assertThrows(IllegalStateException.class, () -> new AuthPacketDefinition(parser, handler));
    }
}
