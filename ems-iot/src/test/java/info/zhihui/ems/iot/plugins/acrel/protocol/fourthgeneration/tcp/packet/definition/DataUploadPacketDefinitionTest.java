package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.DataUploadPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.DataUploadPacketParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataUploadPacketDefinitionTest {

    @Test
    void testDataUploadDefinition_DelegatesToParserAndHandler() {
        DataUploadPacketParser parser = Mockito.mock(DataUploadPacketParser.class);
        DataUploadPacketHandler handler = Mockito.mock(DataUploadPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD));
        Mockito.when(handler.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD));

        DataUploadPacketDefinition definition = new DataUploadPacketDefinition(parser, handler);
        byte[] payload = new byte[]{0x03};
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext();
        AcrelMessage message = Mockito.mock(AcrelMessage.class);
        Mockito.when(parser.parse(context, payload)).thenReturn(message);

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD), definition.command());
        Assertions.assertSame(message, definition.parse(context, payload));
        definition.handle(context, message);
        Mockito.verify(handler).handle(context, message);
    }
}
