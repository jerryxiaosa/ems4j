package info.zhihui.ems.iot.domain.model;

import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设备命令响应结果。
 */
@Data
@Accessors(chain = true)
public class DeviceCommandResult {
    private DeviceCommandTypeEnum type;
    private boolean success;
    private Object data;
    private byte[] rawPayload;
    private String errorMessage;
}
