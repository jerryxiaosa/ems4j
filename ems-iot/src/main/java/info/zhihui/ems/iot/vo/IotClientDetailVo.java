package info.zhihui.ems.iot.vo;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class IotClientDetailVo {

    private String channelId;

    private String deviceNo;

    private DeviceTypeEnum deviceType;

    private boolean active;

    private boolean open;

    private boolean registered;

    private boolean writable;

    private boolean sending;

    private boolean pending;

    private int queueSize;

    private int abnormalCount;

    private String remoteAddress;

    private String localAddress;

    private String productCode;

    private DeviceAccessModeEnum accessMode;

    private Integer parentId;

    private Integer portNo;

    private Integer meterAddress;

    private LocalDateTime lastOnlineAt;
}
