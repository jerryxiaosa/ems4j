package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;

/**
 * 断电命令。
 */
public class CutOffCommand implements DeviceCommandRequest {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.CUT_OFF;
    }
}
