package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayTransparentMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 网关透传应答处理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DownlinkAckPacketHandler implements GatewayPacketHandler {

    private final AcrelGatewayDeviceResolver deviceResolver;
    private final ProtocolCommandTransport commandTransport;

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        GatewayTransparentMessage payload = (GatewayTransparentMessage) message;
        if (payload == null) {
            return;
        }
        Device gateway = deviceResolver.resolveGateway(context);
        if (gateway == null) {
            log.warn("透传应答未绑定网关，session={}", sessionId(context));
            return;
        }
        commandTransport.completePending(gateway.getDeviceNo(), payload.payload());
    }

    private String sessionId(ProtocolMessageContext context) {
        if (context == null) {
            return null;
        }
        ProtocolSession session = context.getSession();
        return session == null ? null : session.getSessionId();
    }
}
