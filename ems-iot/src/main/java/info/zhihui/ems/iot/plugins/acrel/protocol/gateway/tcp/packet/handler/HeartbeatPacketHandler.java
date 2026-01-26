package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolInboundPublisher;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 网关心跳报文处理器。
 */
@Slf4j
@Component("gatewayHeartbeatPacketHandler")
@RequiredArgsConstructor
public class HeartbeatPacketHandler implements GatewayPacketHandler {

    private static final DateTimeFormatter HEARTBEAT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AcrelGatewayDeviceResolver deviceResolver;
    private final ProtocolInboundPublisher protocolInboundPublisher;
    private final AcrelGatewayFrameCodec frameCodec;

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.HEARTBEAT);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        Device gateway = deviceResolver.resolveGateway(context);
        if (gateway == null) {
            log.warn("网关心跳未绑定设备，session={}", sessionId(context));
            return;
        }
        String deviceNo = gateway.getDeviceNo();
        if (StringUtils.isBlank(deviceNo)) {
            log.warn("网关心跳缺少 deviceNo，session={}", sessionId(context));
            return;
        }
        try {
            LocalDateTime receivedAt = context.getReceivedAt() != null ? context.getReceivedAt() : LocalDateTime.now();
            ProtocolHeartbeatInboundEvent event = new ProtocolHeartbeatInboundEvent()
                    .setDeviceNo(deviceNo)
                    .setSessionId(sessionId(context))
                    .setReceivedAt(receivedAt)
                    .setTransportType(context.getTransportType())
                    .setRawPayloadHex(HexUtil.bytesToHexString(context.getRawPayload()));
            protocolInboundPublisher.publish(event);
            log.debug("收到网关心跳 {}，session={}", deviceNo, sessionId(context));
        } catch (Exception ex) {
            log.warn("网关心跳事件发布异常 deviceNo={} session={}", deviceNo, sessionId(context), ex);
        }

        String time = HEARTBEAT_TIME_FORMAT.format(LocalDateTime.now());
        String xml = buildHeartbeatXml(gateway.getDeviceNo(), time);
        byte[] frame = frameCodec.encode(GatewayPacketCode.HEARTBEAT, xml.getBytes(StandardCharsets.UTF_8));
        ProtocolSession session = context.getSession();
        if (session != null) {
            session.send(frame);
            log.debug("网关心跳响应成功，网关：{}，channel：{}，时间：{}", gateway.getDeviceNo(), sessionId(context), time);
        }
    }

    private String buildHeartbeatXml(String gatewayId, String time) {
        String safeGatewayId = safeValue(gatewayId);
        String safeTime = safeValue(time);
        return "<?xml version=\"1.0\"?><root><common><building_id></building_id><gateway_id>"
                + safeGatewayId + "</gateway_id><type>heart_beat</type></common><heart_beat operation=\"time\"><time>"
                + safeTime + "</time></heart_beat></root>";
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String sessionId(ProtocolMessageContext context) {
        if (context == null) {
            return null;
        }
        ProtocolSession session = context.getSession();
        return session == null ? null : session.getSessionId();
    }
}
