package info.zhihui.ems.iot.protocol.event.inbound;

import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Inbound energy report event from protocol layer.
 */
@Data
@Accessors(chain = true)
public class ProtocolEnergyReportInboundEvent implements ProtocolInboundEvent {
    private String deviceNo;
    private String gatewayDeviceNo;
    private String sessionId;
    private String meterAddress;
    private int totalEnergy;
    private int higherEnergy;
    private int highEnergy;
    private int lowEnergy;
    private int lowerEnergy;
    private int deepLowEnergy;
    private LocalDateTime reportedAt;
    private LocalDateTime receivedAt;
    private TransportProtocolEnum transportType;
    private String rawPayload;
}
