package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolInboundPublisher;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 4G 心跳命令处理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatPacketHandler implements Acrel4gPacketHandler {

    private final ProtocolInboundPublisher protocolInboundPublisher;

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.HEARTBEAT);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        ProtocolSession session = context == null ? null : context.getSession();
        if (session == null) {
            return;
        }
        String deviceNo = context.getDeviceNo();

        if (StringUtils.isBlank(deviceNo)) {
            log.warn("4G 心跳缺少 deviceNo，session={}", session.getSessionId());
            return;
        }
        try {
            LocalDateTime receivedAt = context.getReceivedAt() != null ? context.getReceivedAt() : LocalDateTime.now();
            ProtocolHeartbeatInboundEvent event = new ProtocolHeartbeatInboundEvent()
                    .setDeviceNo(deviceNo)
                    .setSessionId(session.getSessionId())
                    .setReceivedAt(receivedAt)
                    .setTransportType(context.getTransportType())
                    .setRawPayloadHex(HexUtil.bytesToHexString(context.getRawPayload()));
            protocolInboundPublisher.publish(event);
            log.debug("4G 心跳 {}", deviceNo);
        } catch (Exception e) {
            log.warn("4G 心跳事件发布异常 deviceNo={}", deviceNo, e);
        }
    }
}
