package info.zhihui.ems.foundation.integration.biz.command.dto;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceCommandCancelDto {

    /**
     * 设备ID
     */
    private Integer deviceId;

    /**
     * 设备类型
     */
    private DeviceTypeEnum deviceType;

    /**
     * 操作类型
     */
    private CommandTypeEnum commandType;

    /**
     * 取消原因
     */
    private String reason;
}
