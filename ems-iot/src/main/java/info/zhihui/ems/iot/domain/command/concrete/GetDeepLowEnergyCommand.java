package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 获取深谷电量命令。
 */
@Data
@Accessors(chain = true)
public class GetDeepLowEnergyCommand implements DeviceCommandRequest {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DEEP_LOW_ENERGY;
    }
}
