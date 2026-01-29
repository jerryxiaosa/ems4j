package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler.DataZipPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser.DataZipPacketParser;
import org.springframework.stereotype.Component;

/**
 * 网关压缩数据上报命令定义（0x04）。
 */
@Component
public class DataZipPacketDefinition implements GatewayPacketDefinition {

    private final DataZipPacketParser parser;
    private final DataZipPacketHandler handler;

    public DataZipPacketDefinition(DataZipPacketParser parser, DataZipPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.DATA_ZIP);
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
