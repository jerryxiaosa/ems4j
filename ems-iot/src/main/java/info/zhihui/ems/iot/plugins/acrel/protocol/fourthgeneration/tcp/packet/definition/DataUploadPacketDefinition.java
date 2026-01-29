package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler.DataUploadPacketHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser.DataUploadPacketParser;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import org.springframework.stereotype.Component;

/**
 * 数据上报命令定义。
 */
@Component
public class DataUploadPacketDefinition implements Acrel4gPacketDefinition {

    private final DataUploadPacketParser parser;
    private final DataUploadPacketHandler handler;

    public DataUploadPacketDefinition(DataUploadPacketParser parser, DataUploadPacketHandler handler) {
        this.parser = parser;
        this.handler = handler;
        validate(parser.command(), handler.command());
    }

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD);
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
