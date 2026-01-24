package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayXmlParser;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayDataMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayReportMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 网关压缩数据上报解析器（0x04）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataZipPacketParser implements GatewayPacketParser {

    private final AcrelGatewayCryptoService cryptoService;
    private final AcrelGatewayXmlParser xmlParser;
    private final AcrelGatewayDeviceResolver deviceResolver;

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.DATA_ZIP);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        Device gateway = deviceResolver.resolveGateway(context);
        if (gateway == null || !StringUtils.hasText(gateway.getDeviceSecret())) {
            log.warn("网关密钥缺失，无法解析压缩数据，session={}", sessionId(context));
            return null;
        }
        try {
            byte[] decrypted = cryptoService.decrypt(payload, gateway.getDeviceSecret());
            byte[] xmlBytes = cryptoService.unzip(decrypted);
            String xml = new String(xmlBytes, StandardCharsets.UTF_8);
            GatewayReportMessage report = xmlParser.parseReport(xml);
            if (report == null) {
                log.warn("网关压缩数据解析失败，gateway={}", gateway.getDeviceNo());
                return null;
            }
            return new GatewayDataMessage(report, xml);
        } catch (Exception ex) {
            log.warn("网关压缩数据解密/解压失败，session={} gateway={} payloadSize={}",
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
