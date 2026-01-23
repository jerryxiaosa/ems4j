package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DownlinkAckMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 4G 下发应答命令处理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DownlinkPacketHandler implements Acrel4gPacketHandler {

    private final ProtocolCommandTransport commandTransport;

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DOWNLINK);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        ProtocolSession session = context == null ? null : context.getSession();
        if (session == null) {
            return;
        }
        DownlinkAckMessage ack = (DownlinkAckMessage) message;
        String deviceNo = ack.getSerialNumber();
        if (StringUtils.isBlank(deviceNo)) {
            deviceNo = context.getDeviceNo();
        }
        if (StringUtils.isBlank(deviceNo)) {
            log.warn("4G 下发应答缺少设备编号，session={}", session.getSessionId());
            return;
        }
        byte[] modbusFrame = ack.getModbusFrame();
        commandTransport.completePending(deviceNo, modbusFrame == null ? context.getRawPayload() : modbusFrame);
    }
}
