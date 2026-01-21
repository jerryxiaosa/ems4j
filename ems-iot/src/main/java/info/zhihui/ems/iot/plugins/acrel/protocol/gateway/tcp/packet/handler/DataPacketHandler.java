package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.ProtocolInboundPublisher;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayDataMessage;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayReportMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.MeterEnergyPayload;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayMeterIdCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 网关数据上报处理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPacketHandler implements GatewayPacketHandler {

    private final AcrelGatewayDeviceResolver deviceResolver;
    private final DeviceRegistry deviceRegistry;
    private final ProtocolInboundPublisher protocolInboundPublisher;

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.DATA);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        Device gateway = deviceResolver.resolveGateway(context);
        if (gateway == null) {
            log.warn("网关数据未绑定设备，session={}", sessionId(context));
            return;
        }
        GatewayDataMessage dataMessage = (GatewayDataMessage) message;
        GatewayReportMessage report = dataMessage.report();
        if (report == null) {
            return;
        }
        publishEnergyEvents(context, gateway, report, dataMessage.rawXml());
    }

    private void publishEnergyEvents(ProtocolMessageContext context, Device gateway,
                                     GatewayReportMessage report, String rawXml) {
        List<MeterEnergyPayload> meters = report.meters();
        if (meters == null || meters.isEmpty()) {
            return;
        }
        for (MeterEnergyPayload meter : meters) {
            if (!StringUtils.hasText(meter.meterId())) {
                continue;
            }
            AcrelGatewayMeterIdCodec.MeterIdentity identity = AcrelGatewayMeterIdCodec.parse(meter.meterId());
            if (identity == null) {
                log.warn("网关电表标识格式不正确，gateway={} meterId={}", gateway.getDeviceNo(), meter.meterId());
                continue;
            }
            Device device;
            try {
                device = deviceRegistry.getByParentIdAndPortNoAndMeterAddress(gateway.getId(),
                        identity.portNo(), identity.meterAddress());
            } catch (Exception ex) {
                log.warn("网关电表未注册，gateway={} meterId={}", gateway.getDeviceNo(), meter.meterId());
                continue;
            }
            int totalEnergy = toEnergyInt(meter.totalEnergy());
            ProtocolEnergyReportInboundEvent event = new ProtocolEnergyReportInboundEvent()
                    .setDeviceNo(device.getDeviceNo())
                    .setGatewayDeviceNo(gateway.getDeviceNo())
                    .setMeterAddress(String.valueOf(identity.meterAddress()))
                    .setTotalEnergy(totalEnergy)
                    .setReportedAt(resolveReportedAt(report.reportedAt()))
                    .setReceivedAt(resolveReceivedAt(context))
                    .setTransportType(context.getTransportType())
                    .setSessionId(sessionId(context))
                    .setRawPayload(buildRawPayload(HexUtil.bytesToHexString(context.getRawPayload()), rawXml));
            try {
                protocolInboundPublisher.publish(event);
            } catch (RuntimeException ex) {
                log.warn("网关能耗上报事件发布异常 gateway={} meterId={}", gateway.getDeviceNo(), meter.meterId(), ex);
            }
        }
    }

    private LocalDateTime resolveReportedAt(LocalDateTime reportedAt) {
        return reportedAt == null ? LocalDateTime.now() : reportedAt;
    }

    private LocalDateTime resolveReceivedAt(ProtocolMessageContext context) {
        return context.getReceivedAt() == null ? LocalDateTime.now() : context.getReceivedAt();
    }

    private int toEnergyInt(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        return value.setScale(0, RoundingMode.DOWN).intValue();
    }

    private String buildRawPayload(String rawHex, String rawXml) {
        boolean hasHex = StringUtils.hasText(rawHex);
        boolean hasXml = StringUtils.hasText(rawXml);
        if (hasHex && hasXml) {
            return "hex=" + rawHex + ";xml=" + rawXml;
        }
        if (hasHex) {
            return rawHex;
        }
        return hasXml ? rawXml : null;
    }

    private String sessionId(ProtocolMessageContext context) {
        if (context == null) {
            return null;
        }
        ProtocolSession session = context.getSession();
        return session == null ? null : session.getSessionId();
    }
}
