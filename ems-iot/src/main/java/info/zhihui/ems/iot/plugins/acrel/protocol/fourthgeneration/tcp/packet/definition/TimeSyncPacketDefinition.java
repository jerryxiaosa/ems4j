package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.TimeSyncPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.TimeSyncPacketParser;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import org.springframework.stereotype.Component;

/**
 * 对时命令定义。
 */
@Component
public class TimeSyncPacketDefinition implements Acrel4gPacketDefinition {

    private final TimeSyncPacketParser parser;
    private final TimeSyncPacketHandler handler;

    public TimeSyncPacketDefinition(TimeSyncPacketParser parser, TimeSyncPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC);
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
