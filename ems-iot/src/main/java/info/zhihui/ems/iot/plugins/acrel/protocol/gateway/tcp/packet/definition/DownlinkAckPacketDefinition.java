package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler.DownlinkAckPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser.DownlinkAckPacketParser;
import org.springframework.stereotype.Component;

/**
 * 网关透传应答命令定义（0xF2）。
 */
@Component
public class DownlinkAckPacketDefinition implements GatewayPacketDefinition {

    private final DownlinkAckPacketParser parser;
    private final DownlinkAckPacketHandler handler;

    public DownlinkAckPacketDefinition(DownlinkAckPacketParser parser, DownlinkAckPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK);
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
