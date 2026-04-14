package info.zhihui.ems.iot.vo;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IotClientSimpleVo {

    private String channelId;

    private String deviceNo;

    private DeviceTypeEnum deviceType;

    private boolean active;

    private boolean pending;

    private int queueSize;

    private int abnormalCount;

    private String remoteAddress;
}
