package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.HeartbeatPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.HeartbeatPacketParser;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import org.springframework.stereotype.Component;

/**
 * 心跳命令定义。
 */
@Component
public class HeartbeatPacketDefinition implements Acrel4gPacketDefinition {

    private final HeartbeatPacketParser parser;
    private final HeartbeatPacketHandler handler;

    public HeartbeatPacketDefinition(HeartbeatPacketParser parser, HeartbeatPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.HEARTBEAT);
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
