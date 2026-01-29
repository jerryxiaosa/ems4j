package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.RegisterPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.RegisterPacketParser;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import org.springframework.stereotype.Component;

/**
 * 注册命令定义。
 */
@Component
public class RegisterPacketDefinition implements Acrel4gPacketDefinition {

    private final RegisterPacketParser parser;
    private final RegisterPacketHandler handler;

    public RegisterPacketDefinition(RegisterPacketParser parser, RegisterPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER);
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
