package info.zhihui.ems.iot.domain.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DeviceEnergyReportEvent {
    private String deviceNo;
    private String gatewayDeviceNo;
    private String meterAddress;
    private int totalEnergy;
    private int higherEnergy;
    private int highEnergy;
    private int lowEnergy;
    private int lowerEnergy;
    private int deepLowEnergy;
    private LocalDateTime reportedAt;
    private LocalDateTime receivedAt;
    private String source;
    private String raw;
}
