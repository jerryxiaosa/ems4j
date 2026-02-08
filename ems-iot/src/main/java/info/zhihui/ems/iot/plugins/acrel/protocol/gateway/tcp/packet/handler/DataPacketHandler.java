package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import org.springframework.context.ApplicationEventPublisher;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayDataMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayReportMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.MeterEnergyPayload;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayMeterIdCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    private final ApplicationEventPublisher protocolInboundPublisher;

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
            log.debug("网关数据上报，gateway={} meterId={}", gateway.getDeviceNo(), meter.meterId());
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
            BigDecimal totalEnergy = meter.totalEnergy();
            BigDecimal higherEnergy = meter.higherEnergy();
            BigDecimal highEnergy = meter.highEnergy();
            BigDecimal lowEnergy = meter.lowEnergy();
            BigDecimal lowerEnergy = meter.lowerEnergy();
            BigDecimal deepLowEnergy = meter.deepLowEnergy();
            LocalDateTime receivedAt = resolveReceivedAt(context);
            LocalDateTime reportedAt =report.reportedAt() != null ? report.reportedAt() : receivedAt;

            ProtocolEnergyReportInboundEvent event = new ProtocolEnergyReportInboundEvent()
                    .setDeviceNo(device.getDeviceNo())
                    .setGatewayDeviceNo(gateway.getDeviceNo())
                    .setMeterAddress(String.valueOf(identity.meterAddress()))
                    .setTotalEnergy(totalEnergy)
                    .setHigherEnergy(higherEnergy)
                    .setHighEnergy(highEnergy)
                    .setLowEnergy(lowEnergy)
                    .setLowerEnergy(lowerEnergy)
                    .setDeepLowEnergy(deepLowEnergy)
                    .setReportedAt(reportedAt)
                    .setReceivedAt(receivedAt)
                    .setTransportType(context.getTransportType())
                    .setSessionId(sessionId(context))
                    .setRawPayload(buildRawPayload(HexUtil.bytesToHexString(context.getRawPayload()), rawXml));
            try {
                protocolInboundPublisher.publishEvent(event);
            } catch (RuntimeException ex) {
                log.warn("网关能耗上报事件发布异常 gateway={} meterId={}", gateway.getDeviceNo(), meter.meterId(), ex);
            }
        }
    }

    private LocalDateTime resolveReceivedAt(ProtocolMessageContext context) {
        return context.getReceivedAt() == null ? LocalDateTime.now() : context.getReceivedAt();
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
