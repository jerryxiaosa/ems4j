package info.zhihui.ems.iot.listener;

import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.iot.config.IotEnergyReportPushProperties;
import info.zhihui.ems.iot.domain.event.DeviceEnergyReportEvent;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import info.zhihui.ems.iot.vo.StandardEnergyReportPushVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Component
@Slf4j
public class ProtocolInboundEventListener {

    private final DeviceRegistry deviceRegistry;
    private final IotEnergyReportPushProperties energyReportPushProperties;
    private final RestClient iotRestClient;

    public ProtocolInboundEventListener(DeviceRegistry deviceRegistry,
                                        IotEnergyReportPushProperties energyReportPushProperties,
                                        @Qualifier("iotRestClient") RestClient iotRestClient) {
        this.deviceRegistry = deviceRegistry;
        this.energyReportPushProperties = energyReportPushProperties;
        this.iotRestClient = iotRestClient;
    }

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
        DeviceEnergyReportEvent reportEvent = buildDeviceEnergyReportEvent(event, receivedAt, reportedAt);
        log.info("---- 能耗上报: {}", reportEvent);
        if (!energyReportPushProperties.isEnabled()) {
            return;
        }
        if (!validateEnergyReport(reportEvent)) {
            log.warn("能耗上报字段不完整，跳过推送，deviceNo={} reportedAt={}",
                    reportEvent.getDeviceNo(), reportEvent.getReportedAt());
            return;
        }
        try {
            pushEnergyReport(reportEvent);
        } catch (Exception ex) {
            log.error("能耗上报处理异常，deviceNo={}", reportEvent.getDeviceNo(), ex);
        }
    }

    private DeviceEnergyReportEvent buildDeviceEnergyReportEvent(ProtocolEnergyReportInboundEvent event,
                                                                 LocalDateTime receivedAt,
                                                                 LocalDateTime reportedAt) {
        return new DeviceEnergyReportEvent()
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
    }

    private boolean validateEnergyReport(DeviceEnergyReportEvent reportEvent) {
        return reportEvent.getReportedAt() != null
                && reportEvent.getTotalEnergy() != null
                && reportEvent.getHigherEnergy() != null
                && reportEvent.getHighEnergy() != null
                && reportEvent.getLowEnergy() != null
                && reportEvent.getLowerEnergy() != null
                && reportEvent.getDeepLowEnergy() != null;
    }

    private void pushEnergyReport(DeviceEnergyReportEvent reportEvent) {
        if (StringUtils.isBlank(energyReportPushProperties.getBaseUrl())) {
            log.error("能耗上报推送失败：baseUrl未配置，deviceNo={}", reportEvent.getDeviceNo());
            return;
        }
        StandardEnergyReportPushVo pushVo = buildPushVo(reportEvent);
        String pushUrl = buildPushUrl();
        try {
            RestResult<Object> result = iotRestClient.post()
                    .uri(pushUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(pushVo)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (result == null) {
                log.error("能耗上报推送失败：响应为空，deviceNo={} url={}", reportEvent.getDeviceNo(), pushUrl);
                return;
            }
            if (!Boolean.TRUE.equals(result.getSuccess())) {
                log.warn("能耗上报推送业务失败，deviceNo={} code={} message={}",
                        reportEvent.getDeviceNo(), result.getCode(), result.getMessage());
            }
        } catch (RestClientResponseException ex) {
            log.error("能耗上报推送失败（HTTP异常，可重试），deviceNo={} status={} body={}",
                    reportEvent.getDeviceNo(), ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            log.error("能耗上报推送失败（网络异常，可重试），deviceNo={}", reportEvent.getDeviceNo(), ex);
        }
    }

    private StandardEnergyReportPushVo buildPushVo(DeviceEnergyReportEvent reportEvent) {
        return new StandardEnergyReportPushVo()
                .setSource(resolveSource(reportEvent))
                .setSourceReportId(buildSourceReportId(reportEvent))
                .setDeviceNo(reportEvent.getDeviceNo())
                .setRecordTime(reportEvent.getReportedAt())
                .setTotalEnergy(reportEvent.getTotalEnergy())
                .setHigherEnergy(reportEvent.getHigherEnergy())
                .setHighEnergy(reportEvent.getHighEnergy())
                .setLowEnergy(reportEvent.getLowEnergy())
                .setLowerEnergy(reportEvent.getLowerEnergy())
                .setDeepLowEnergy(reportEvent.getDeepLowEnergy());
    }

    private String resolveSource(DeviceEnergyReportEvent reportEvent) {
        if (StringUtils.isNotBlank(energyReportPushProperties.getSource())) {
            return energyReportPushProperties.getSource();
        }
        if (StringUtils.isNotBlank(reportEvent.getSource())) {
            return reportEvent.getSource();
        }
        return "IOT";
    }

    private String buildSourceReportId(DeviceEnergyReportEvent reportEvent) {
        String sourceSeed = String.join("|",
                StringUtils.defaultString(reportEvent.getDeviceNo()),
                String.valueOf(reportEvent.getReportedAt()),
                normalizeDecimal(reportEvent.getTotalEnergy()),
                normalizeDecimal(reportEvent.getHigherEnergy()),
                normalizeDecimal(reportEvent.getHighEnergy()),
                normalizeDecimal(reportEvent.getLowEnergy()),
                normalizeDecimal(reportEvent.getLowerEnergy()),
                normalizeDecimal(reportEvent.getDeepLowEnergy()),
                StringUtils.defaultString(reportEvent.getRaw()));
        return sha256Hex(sourceSeed);
    }

    private String normalizeDecimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private String sha256Hex(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("不支持SHA-256算法", ex);
        }
    }

    private String buildPushUrl() {
        String baseUrl = StringUtils.defaultString(energyReportPushProperties.getBaseUrl());
        String path = StringUtils.defaultIfBlank(energyReportPushProperties.getPath(), "/device/energy-reports/standard");
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + (path.startsWith("/") ? path : "/" + path);
    }
}
