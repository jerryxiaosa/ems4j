package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.DownlinkPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.DownlinkPacketParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DownlinkPacketDefinitionTest {

    @Test
    void testDownlinkDefinition_DelegatesToParserAndHandler() {
        DownlinkPacketParser parser = Mockito.mock(DownlinkPacketParser.class);
        DownlinkPacketHandler handler = Mockito.mock(DownlinkPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DOWNLINK));
        Mockito.when(handler.command()).thenReturn(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DOWNLINK));

        DownlinkPacketDefinition definition = new DownlinkPacketDefinition(parser, handler);
        byte[] payload = new byte[]{0x01};
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext();
        AcrelMessage message = Mockito.mock(AcrelMessage.class);
        Mockito.when(parser.parse(context, payload)).thenReturn(message);

        Assertions.assertEquals(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DOWNLINK), definition.command());
        Assertions.assertSame(message, definition.parse(context, payload));
        definition.handle(context, message);
        Mockito.verify(handler).handle(context, message);
    }

    @Test
    void testDownlinkDefinition_WhenCommandMismatch_ShouldThrow() {
        DownlinkPacketParser parser = Mockito.mock(DownlinkPacketParser.class);
        DownlinkPacketHandler handler = Mockito.mock(DownlinkPacketHandler.class);
        Mockito.when(parser.command()).thenReturn(AcrelPacketKeySupport.commandKey((byte) 0x00));
        Mockito.when(handler.command()).thenReturn(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DOWNLINK));

        Assertions.assertThrows(IllegalStateException.class, () -> new DownlinkPacketDefinition(parser, handler));
    }
}
