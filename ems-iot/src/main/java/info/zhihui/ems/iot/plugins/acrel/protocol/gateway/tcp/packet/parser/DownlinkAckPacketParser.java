package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayTransparentCodec;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayTransparentMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 网关透传应答解析器（0xF2）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DownlinkAckPacketParser implements GatewayPacketParser {

    private final AcrelGatewayCryptoService cryptoService;
    private final AcrelGatewayTransparentCodec transparentCodec;
    private final AcrelGatewayDeviceResolver deviceResolver;

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.DOWNLINK_ACK);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        Device gateway = deviceResolver.resolveGateway(context);
        if (gateway == null || !StringUtils.hasText(gateway.getDeviceSecret())) {
            log.warn("网关密钥缺失，无法解析透传应答，session={}", sessionId(context));
            return null;
        }
        try {
            byte[] decrypted = cryptoService.decrypt(payload, gateway.getDeviceSecret());
            GatewayTransparentMessage message = transparentCodec.decode(new String(decrypted, StandardCharsets.UTF_8));
            if (message == null) {
                log.warn("透传应答解析失败，gateway={}", gateway.getDeviceNo());
                return null;
            }
            return message;
        } catch (Exception ex) {
            log.warn("透传应答解密失败，session={} gateway={} payloadSize={}",
                    sessionId(context), gateway.getDeviceNo(), payload == null ? 0 : payload.length, ex);
            return null;
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
