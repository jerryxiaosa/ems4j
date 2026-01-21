package info.zhihui.ems.business.device.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设备状态同步请求参数。
 */
@Data
@Accessors(chain = true)
public class DeviceStatusSyncRequestDto {

    /**
     * 是否强制写入状态。
     */
    private Boolean force;

    /**
     * 强制状态。
     */
    private Boolean onlineStatus;

}
