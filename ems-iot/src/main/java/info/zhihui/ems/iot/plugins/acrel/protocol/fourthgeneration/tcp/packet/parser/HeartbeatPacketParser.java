package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.HeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import org.springframework.stereotype.Component;

/**
 * 心跳命令解析器（0x94）。
 */
@Component
public class HeartbeatPacketParser implements Acrel4gPacketParser {

    @Override
    public String command() {
        return AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.HEARTBEAT);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        return new HeartbeatMessage();
    }
}
