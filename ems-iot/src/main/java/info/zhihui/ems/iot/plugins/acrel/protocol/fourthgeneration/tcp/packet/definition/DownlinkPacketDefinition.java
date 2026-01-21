package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
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
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DOWNLINK);
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
