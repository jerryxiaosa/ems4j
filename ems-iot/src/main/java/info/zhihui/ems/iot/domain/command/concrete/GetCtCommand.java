package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;

/**
 * 读取 CT 命令。
 */
public class GetCtCommand implements DeviceCommandRequest {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_CT;
    }
}
