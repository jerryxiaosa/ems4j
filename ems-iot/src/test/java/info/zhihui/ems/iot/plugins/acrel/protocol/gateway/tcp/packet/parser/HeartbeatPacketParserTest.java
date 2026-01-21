package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser;

import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayHeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HeartbeatPacketParserTest {

    @Test
    void command_shouldReturnHeartbeat() {
        HeartbeatPacketParser parser = new HeartbeatPacketParser();

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT), parser.command());
    }

    @Test
    void parse_shouldReturnHeartbeatMessage() {
        HeartbeatPacketParser parser = new HeartbeatPacketParser();

        GatewayHeartbeatMessage message = (GatewayHeartbeatMessage) parser.parse(null, new byte[]{0x01});

        Assertions.assertNotNull(message);
    }
}
