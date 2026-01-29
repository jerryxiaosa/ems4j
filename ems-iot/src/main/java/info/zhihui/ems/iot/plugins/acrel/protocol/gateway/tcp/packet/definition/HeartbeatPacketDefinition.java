package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler.HeartbeatPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser.HeartbeatPacketParser;
import org.springframework.stereotype.Component;

/**
 * 网关心跳命令定义。
 */
@Component("gatewayHeartbeatPacketDefinition")
public class HeartbeatPacketDefinition implements GatewayPacketDefinition {

    private final HeartbeatPacketParser parser;
    private final HeartbeatPacketHandler handler;

    public HeartbeatPacketDefinition(HeartbeatPacketParser parser, HeartbeatPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        return parser.parse(context, payload);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        handler.handle(context, message);
    }
}
