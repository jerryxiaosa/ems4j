package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.DownlinkPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.DownlinkPacketParser;
import org.springframework.stereotype.Component;

/**
 * 下发应答命令定义。
 */
@Component
public class DownlinkPacketDefinition implements Acrel4gPacketDefinition {

    private final DownlinkPacketParser parser;
    private final DownlinkPacketHandler handler;

    public DownlinkPacketDefinition(DownlinkPacketParser parser, DownlinkPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DOWNLINK);
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
