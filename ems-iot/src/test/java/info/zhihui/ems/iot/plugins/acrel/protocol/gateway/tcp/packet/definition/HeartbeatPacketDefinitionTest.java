package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler.HeartbeatPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser.HeartbeatPacketParser;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HeartbeatPacketDefinitionTest {

    @Test
    void command_shouldReturnHeartbeat() {
        HeartbeatPacketParser parser = Mockito.mock(HeartbeatPacketParser.class);
        HeartbeatPacketHandler handler = Mockito.mock(HeartbeatPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT));
        HeartbeatPacketDefinition definition = new HeartbeatPacketDefinition(parser, handler);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT), definition.command());
    }

    @Test
    void parse_shouldDelegateToParser() {
        HeartbeatPacketParser parser = Mockito.mock(HeartbeatPacketParser.class);
        HeartbeatPacketHandler handler = Mockito.mock(HeartbeatPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT));
        HeartbeatPacketDefinition definition = new HeartbeatPacketDefinition(parser, handler);
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
        HeartbeatPacketParser parser = Mockito.mock(HeartbeatPacketParser.class);
        HeartbeatPacketHandler handler = Mockito.mock(HeartbeatPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT));
        HeartbeatPacketDefinition definition = new HeartbeatPacketDefinition(parser, handler);
        ProtocolMessageContext context = Mockito.mock(ProtocolMessageContext.class);
        AcrelMessage message = Mockito.mock(AcrelMessage.class);

        definition.handle(context, message);

        Mockito.verify(handler).handle(context, message);
    }

    @Test
    void constructor_whenCommandMismatch_shouldThrow() {
        HeartbeatPacketParser parser = Mockito.mock(HeartbeatPacketParser.class);
        HeartbeatPacketHandler handler = Mockito.mock(HeartbeatPacketHandler.class);
        Mockito.when(parser.command()).thenReturn("ff");
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT));

        Assertions.assertThrows(IllegalStateException.class, () -> new HeartbeatPacketDefinition(parser, handler));
    }
}
