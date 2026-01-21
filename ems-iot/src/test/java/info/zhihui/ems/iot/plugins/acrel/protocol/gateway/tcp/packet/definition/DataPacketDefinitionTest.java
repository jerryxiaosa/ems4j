package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler.DataPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser.DataPacketParser;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataPacketDefinitionTest {

    @Test
    void command_shouldReturnData() {
        DataPacketParser parser = Mockito.mock(DataPacketParser.class);
        DataPacketHandler handler = Mockito.mock(DataPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DATA));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DATA));
        DataPacketDefinition definition = new DataPacketDefinition(parser, handler);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.DATA), definition.command());
    }

    @Test
    void parse_shouldDelegateToParser() {
        DataPacketParser parser = Mockito.mock(DataPacketParser.class);
        DataPacketHandler handler = Mockito.mock(DataPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DATA));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DATA));
        DataPacketDefinition definition = new DataPacketDefinition(parser, handler);
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
        DataPacketParser parser = Mockito.mock(DataPacketParser.class);
        DataPacketHandler handler = Mockito.mock(DataPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DATA));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DATA));
        DataPacketDefinition definition = new DataPacketDefinition(parser, handler);
        ProtocolMessageContext context = Mockito.mock(ProtocolMessageContext.class);
        AcrelMessage message = Mockito.mock(AcrelMessage.class);

        definition.handle(context, message);

        Mockito.verify(handler).handle(context, message);
    }

    @Test
    void constructor_whenCommandMismatch_shouldThrow() {
        DataPacketParser parser = Mockito.mock(DataPacketParser.class);
        DataPacketHandler handler = Mockito.mock(DataPacketHandler.class);
        Mockito.when(parser.command()).thenReturn("ff");
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DATA));

        Assertions.assertThrows(IllegalStateException.class, () -> new DataPacketDefinition(parser, handler));
    }
}
