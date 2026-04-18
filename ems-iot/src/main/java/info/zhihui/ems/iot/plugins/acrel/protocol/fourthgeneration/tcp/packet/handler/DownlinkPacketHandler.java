package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DownlinkAckMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 4G 下发应答命令处理器。
 */
@Component
@RequiredArgsConstructor
public class DownlinkPacketHandler implements Acrel4gPacketHandler {

    private final ProtocolCommandTransport commandTransport;

    @Override
    public String command() {
        return AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DOWNLINK);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        ProtocolSession session = context == null ? null : context.getSession();
        if (session == null) {
            return;
        }
        DownlinkAckMessage ack = (DownlinkAckMessage) message;
        byte[] modbusFrame = ack.getModbusFrame();
        commandTransport.completePending(session, modbusFrame == null ? context.getRawPayload() : modbusFrame);
    }
}
