package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.RegisterPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.RegisterPacketParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RegisterPacketDefinitionTest {

    @Test
    void testRegisterDefinition_DelegatesToParserAndHandler() {
        RegisterPacketParser parser = Mockito.mock(RegisterPacketParser.class);
        RegisterPacketHandler handler = Mockito.mock(RegisterPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER));
        Mockito.when(handler.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER));

        RegisterPacketDefinition definition = new RegisterPacketDefinition(parser, handler);
        byte[] payload = new byte[]{0x01};
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext();
        AcrelMessage message = Mockito.mock(AcrelMessage.class);
        Mockito.when(parser.parse(context, payload)).thenReturn(message);

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER), definition.command());
        Assertions.assertSame(message, definition.parse(context, payload));
        definition.handle(context, message);
        Mockito.verify(handler).handle(context, message);
    }

    @Test
    void testRegisterDefinition_WhenCommandMismatch_ShouldThrow() {
        RegisterPacketParser parser = Mockito.mock(RegisterPacketParser.class);
        RegisterPacketHandler handler = Mockito.mock(RegisterPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(Acrel4gPacketCode.commandKey((byte) 0x00));
        Mockito.when(handler.command()).thenReturn(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER));

        Assertions.assertThrows(IllegalStateException.class, () -> new RegisterPacketDefinition(parser, handler));
    }
}
