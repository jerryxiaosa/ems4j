package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.CommonProtocolSessionKeys;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayAuthMessage;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 网关认证报文处理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthPacketHandler implements GatewayPacketHandler {

    private static final String OPERATION_REQUEST = "request";
    private static final String OPERATION_MD5 = "md5";
    private static final String OPERATION_RESULT = "result";

    private final AcrelGatewayDeviceResolver deviceResolver;
    private final AcrelGatewayCryptoService cryptoService;
    private final AcrelGatewayFrameCodec frameCodec;

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.AUTH);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        ProtocolSession session = context == null ? null : context.getSession();
        if (session == null) {
            return;
        }
        GatewayAuthMessage payload = (GatewayAuthMessage) message;
        if (payload == null || !StringUtils.hasText(payload.gatewayId())) {
            log.warn("网关认证报文缺少 gatewayId，session={}", session.getSessionId());
            return;
        }
        Device gateway = deviceResolver.bindGateway(context, payload.gatewayId());
        if (gateway == null) {
            return;
        }
        String operation = payload.operation();
        if (OPERATION_REQUEST.equalsIgnoreCase(operation)) {
            String sequence = buildSequence();
            session.setAttribute(CommonProtocolSessionKeys.GATEWAY_AUTH_SEQUENCE, sequence);
            sendAuthSequence(context, payload, sequence);
            return;
        }
        if (OPERATION_MD5.equalsIgnoreCase(operation)) {
            String expected;
            try {
                expected = buildMd5(gateway.getDeviceSecret(),
                        session.getAttribute(CommonProtocolSessionKeys.GATEWAY_AUTH_SEQUENCE));
            } catch (Exception ex) {
                log.warn("网关认证 MD5 计算失败，gatewayId={} session={}", payload.gatewayId(), session.getSessionId(), ex);
                sendAuthResult(context, payload, false);
                session.close();
                return;
            }
            boolean pass = expected != null && expected.equalsIgnoreCase(payload.md5());
            sendAuthResult(context, payload, pass);
            if (!pass) {
                session.close();
            }
            return;
        }
        if (OPERATION_RESULT.equalsIgnoreCase(operation)) {
            log.info("网关认证结果: gatewayId={} result={}", payload.gatewayId(), payload.md5());
        }
    }

    private void sendAuthSequence(ProtocolMessageContext context, GatewayAuthMessage payload, String sequence) {
        String xml = buildAuthSequenceXml(payload, sequence);
        byte[] frame = frameCodec.encode(GatewayPacketCode.AUTH, xml.getBytes(StandardCharsets.UTF_8));
        ProtocolSession session = context == null ? null : context.getSession();
        if (session != null) {
            session.send(frame);
        }
    }

    private void sendAuthResult(ProtocolMessageContext context, GatewayAuthMessage payload, boolean pass) {
        String xml = buildAuthResultXml(payload, pass);
        byte[] frame = frameCodec.encode(GatewayPacketCode.AUTH, xml.getBytes(StandardCharsets.UTF_8));
        ProtocolSession session = context == null ? null : context.getSession();
        if (session != null) {
            session.send(frame);
        }
    }

    private String buildAuthSequenceXml(GatewayAuthMessage payload, String sequence) {
        String buildingId = safeValue(payload.buildingId());
        String gatewayId = safeValue(payload.gatewayId());
        return "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
                "<root><common>" +
                "<building_id>" + buildingId + "</building_id>" +
                "<gateway_id>" + gatewayId + "</gateway_id>" +
                "<type>id_validate</type>" +
                "</common>" +
                "<id_validate operation=\"sequence\" version=\"2\">" +
                "<sequence>" + sequence + "</sequence>" +
                "</id_validate></root>";
    }

    private String buildAuthResultXml(GatewayAuthMessage payload, boolean pass) {
        String buildingId = safeValue(payload.buildingId());
        String gatewayId = safeValue(payload.gatewayId());
        String result = pass ? "pass" : "fail";
        return "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
                "<root><common>" +
                "<building_id>" + buildingId + "</building_id>" +
                "<gateway_id>" + gatewayId + "</gateway_id>" +
                "<type>id_validate</type>" +
                "</common>" +
                "<id_validate operation=\"result\" version=\"2\">" +
                "<result>" + result + "</result>" +
                "</id_validate></root>";
    }

    private String buildMd5(String secret, String sequence) {
        if (!StringUtils.hasText(secret) || !StringUtils.hasText(sequence)) {
            return null;
        }
        return cryptoService.md5Hex(secret + sequence);
    }

    private String buildSequence() {
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return String.valueOf(System.currentTimeMillis()) + random;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
