package info.zhihui.ems.iot.simulator.state;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单台模拟设备运行态。
 */
@Data
@Accessors(chain = true)
public class DeviceRuntimeState {

    private String deviceNo;
    private VendorEnum vendor;
    private String productCode;
    private DeviceAccessModeEnum accessMode;
    private String switchStatus;
    private LocalDateTime lastReportedAt;
    private BigDecimal lastTotalEnergy;
    private BigDecimal lastHigherEnergy;
    private BigDecimal lastHighEnergy;
    private BigDecimal lastLowEnergy;
    private BigDecimal lastLowerEnergy;
    private BigDecimal lastDeepLowEnergy;
    private LocalDateTime replayCursorTime;
    private Boolean replayCompleted;
}
