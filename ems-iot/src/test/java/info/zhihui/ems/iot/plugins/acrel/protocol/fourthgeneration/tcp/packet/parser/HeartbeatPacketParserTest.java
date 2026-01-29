package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.HeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HeartbeatPacketParserTest {

    @Test
    void command_shouldReturnHeartbeat() {
        HeartbeatPacketParser parser = new HeartbeatPacketParser();

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.HEARTBEAT), parser.command());
    }

    @Test
    void parse_shouldReturnMessage() {
        HeartbeatPacketParser parser = new HeartbeatPacketParser();

        AcrelMessage message = parser.parse(new SimpleProtocolMessageContext(), null);

        Assertions.assertNotNull(message);
        Assertions.assertInstanceOf(HeartbeatMessage.class, message);
    }

    @Test
    void parse_withContext_shouldDelegateToPayloadParse() {
        HeartbeatPacketParser parser = new HeartbeatPacketParser();

        AcrelMessage message = parser.parse(new SimpleProtocolMessageContext(), new byte[]{0x01});

        Assertions.assertNotNull(message);
        Assertions.assertInstanceOf(HeartbeatMessage.class, message);
    }
}
