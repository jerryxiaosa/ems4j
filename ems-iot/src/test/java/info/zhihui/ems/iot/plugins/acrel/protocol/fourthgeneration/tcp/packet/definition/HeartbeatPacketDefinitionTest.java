package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.HeartbeatPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.HeartbeatPacketParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HeartbeatPacketDefinitionTest {

    @Test
    void testHeartbeatDefinition_DelegatesToParserAndHandler() {
        HeartbeatPacketParser parser = Mockito.mock(HeartbeatPacketParser.class);
        HeartbeatPacketHandler handler = Mockito.mock(HeartbeatPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.HEARTBEAT));
        Mockito.when(handler.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.HEARTBEAT));

        HeartbeatPacketDefinition definition = new HeartbeatPacketDefinition(parser, handler);
        byte[] payload = new byte[]{0x04};
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext();
        AcrelMessage message = Mockito.mock(AcrelMessage.class);
        Mockito.when(parser.parse(context, payload)).thenReturn(message);

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.HEARTBEAT), definition.command());
        Assertions.assertSame(message, definition.parse(context, payload));
        definition.handle(context, message);
        Mockito.verify(handler).handle(context, message);
    }
}
