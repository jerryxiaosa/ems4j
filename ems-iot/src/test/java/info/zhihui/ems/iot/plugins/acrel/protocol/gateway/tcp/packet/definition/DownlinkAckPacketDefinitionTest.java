package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler.DownlinkAckPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser.DownlinkAckPacketParser;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DownlinkAckPacketDefinitionTest {

    @Test
    void command_shouldReturnDownlinkAck() {
        DownlinkAckPacketParser parser = Mockito.mock(DownlinkAckPacketParser.class);
        DownlinkAckPacketHandler handler = Mockito.mock(DownlinkAckPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK));
        DownlinkAckPacketDefinition definition = new DownlinkAckPacketDefinition(parser, handler);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK), definition.command());
    }

    @Test
    void parse_shouldDelegateToParser() {
        DownlinkAckPacketParser parser = Mockito.mock(DownlinkAckPacketParser.class);
        DownlinkAckPacketHandler handler = Mockito.mock(DownlinkAckPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK));
        DownlinkAckPacketDefinition definition = new DownlinkAckPacketDefinition(parser, handler);
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
        DownlinkAckPacketParser parser = Mockito.mock(DownlinkAckPacketParser.class);
        DownlinkAckPacketHandler handler = Mockito.mock(DownlinkAckPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK));
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK));
        DownlinkAckPacketDefinition definition = new DownlinkAckPacketDefinition(parser, handler);
        ProtocolMessageContext context = Mockito.mock(ProtocolMessageContext.class);
        AcrelMessage message = Mockito.mock(AcrelMessage.class);

        definition.handle(context, message);

        Mockito.verify(handler).handle(context, message);
    }

    @Test
    void constructor_whenCommandMismatch_shouldThrow() {
        DownlinkAckPacketParser parser = Mockito.mock(DownlinkAckPacketParser.class);
        DownlinkAckPacketHandler handler = Mockito.mock(DownlinkAckPacketHandler.class);
        Mockito.when(parser.command()).thenReturn("ff");
        Mockito.when(handler.command()).thenReturn(GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK));

        Assertions.assertThrows(IllegalStateException.class, () -> new DownlinkAckPacketDefinition(parser, handler));
    }
}
