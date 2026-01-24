package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayXmlParser;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayAuthMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 网关认证报文解析器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthPacketParser implements GatewayPacketParser {

    private final AcrelGatewayXmlParser xmlParser;

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.AUTH);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        String xml = payload == null ? null : new String(payload, StandardCharsets.UTF_8);
        GatewayAuthMessage message = xmlParser.parseAuth(xml);
        if (message == null) {
            log.warn("网关认证报文解析失败，session={}", sessionId(context));
            return null;
        }
        return message;
    }

    private String sessionId(ProtocolMessageContext context) {
        if (context == null) {
            return null;
        }
        ProtocolSession session = context.getSession();
        return session == null ? null : session.getSessionId();
    }
}
