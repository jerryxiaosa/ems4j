package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.TimeSyncPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.TimeSyncPacketParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TimeSyncPacketDefinitionTest {

    @Test
    void testTimeSyncDefinition_DelegatesToParserAndHandler() {
        TimeSyncPacketParser parser = Mockito.mock(TimeSyncPacketParser.class);
        TimeSyncPacketHandler handler = Mockito.mock(TimeSyncPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC));
        Mockito.when(handler.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC));

        TimeSyncPacketDefinition definition = new TimeSyncPacketDefinition(parser, handler);
        byte[] payload = new byte[]{0x02};
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext();
        AcrelMessage message = Mockito.mock(AcrelMessage.class);
        Mockito.when(parser.parse(context, payload)).thenReturn(message);

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC), definition.command());
        Assertions.assertSame(message, definition.parse(context, payload));
        definition.handle(context, message);
        Mockito.verify(handler).handle(context, message);
    }
}
