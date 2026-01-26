package info.zhihui.ems.iot.application.listener;

import info.zhihui.ems.iot.domain.event.DeviceEnergyReportEvent;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProtocolInboundEventListener {

    private final DeviceRegistry deviceRegistry;

    @EventListener
    public void handleHeartbeat(ProtocolHeartbeatInboundEvent event) {
        if (event == null || StringUtils.isBlank(event.getDeviceNo())) {
            return;
        }
        LocalDateTime receivedAt = event.getReceivedAt() != null ? event.getReceivedAt() : LocalDateTime.now();
        try {
            Device device = deviceRegistry.getByDeviceNo(event.getDeviceNo());
            device.setLastOnlineAt(receivedAt);
            deviceRegistry.update(device);
            log.debug("---- 协议心跳 deviceNo={} session={}", event.getDeviceNo(), event.getSessionId());
        } catch (Exception ex) {
            log.warn("心跳处理异常 deviceNo={} session={}", event.getDeviceNo(), event.getSessionId(), ex);
        }
    }

    @EventListener
    public void handleEnergyReport(ProtocolEnergyReportInboundEvent event) {
        if (event == null || StringUtils.isBlank(event.getDeviceNo())) {
            return;
        }
        LocalDateTime receivedAt = event.getReceivedAt() != null ? event.getReceivedAt() : LocalDateTime.now();
        LocalDateTime reportedAt = event.getReportedAt() != null ? event.getReportedAt() : receivedAt;
        DeviceEnergyReportEvent reportEvent = new DeviceEnergyReportEvent()
                .setDeviceNo(event.getDeviceNo())
                .setGatewayDeviceNo(event.getGatewayDeviceNo())
                .setMeterAddress(event.getMeterAddress())
                .setTotalEnergy(event.getTotalEnergy())
                .setHigherEnergy(event.getHigherEnergy())
                .setHighEnergy(event.getHighEnergy())
                .setLowEnergy(event.getLowEnergy())
                .setLowerEnergy(event.getLowerEnergy())
                .setDeepLowEnergy(event.getDeepLowEnergy())
                .setReportedAt(reportedAt)
                .setReceivedAt(receivedAt)
                .setSource(event.getTransportType() != null ? event.getTransportType().name() : null)
                .setRaw(event.getRawPayload());
        log.info("---- 能耗上报: {}", reportEvent);
    }



}