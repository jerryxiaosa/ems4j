package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler.DataPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser.DataPacketParser;
import org.springframework.stereotype.Component;

/**
 * 网关数据上报命令定义（0x03）。
 */
@Component
public class DataPacketDefinition implements GatewayPacketDefinition {

    private final DataPacketParser parser;
    private final DataPacketHandler handler;

    public DataPacketDefinition(DataPacketParser parser, DataPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.DATA);
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
