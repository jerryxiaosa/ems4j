package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设置 CT 命令。
 */
@Data
@Accessors(chain = true)
public class SetCtCommand implements DeviceCommandRequest {

    private Integer ct;

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_CT;
    }

    @Override
    public void validate() {
        if (ct == null || ct <= 0 || ct > 0xFFFF) {
            throw new BusinessRuntimeException("CT 必须在 1~65535 范围内");
        }
    }
}
