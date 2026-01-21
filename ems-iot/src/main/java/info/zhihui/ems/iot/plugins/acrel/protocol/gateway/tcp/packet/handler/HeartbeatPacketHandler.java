package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final DeviceRegistry deviceRegistry;
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
        try {
            gateway.setLastOnlineAt(LocalDateTime.now());
            deviceRegistry.update(gateway);
        } catch (Exception ex) {
            log.warn("网关心跳更新在线时间失败，gateway={} session={}", gateway.getDeviceNo(), sessionId(context), ex);
        }
        String time = HEARTBEAT_TIME_FORMAT.format(LocalDateTime.now());
        byte[] frame = frameCodec.encode(GatewayPacketCode.HEARTBEAT, time.getBytes(StandardCharsets.UTF_8));
        ProtocolSession session = context.getSession();
        if (session != null) {
            session.send(frame);
        }
    }

    private String sessionId(ProtocolMessageContext context) {
        if (context == null) {
            return null;
        }
        ProtocolSession session = context.getSession();
        return session == null ? null : session.getSessionId();
    }
}
