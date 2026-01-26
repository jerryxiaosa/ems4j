package info.zhihui.ems.iot.domain.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DeviceEnergyReportEvent {
    private String deviceNo;
    private String gatewayDeviceNo;
    private String meterAddress;
    private BigDecimal totalEnergy;
    private BigDecimal higherEnergy;
    private BigDecimal highEnergy;
    private BigDecimal lowEnergy;
    private BigDecimal lowerEnergy;
    private BigDecimal deepLowEnergy;
    private LocalDateTime reportedAt;
    private LocalDateTime receivedAt;
    private String source;
    private String raw;
}
