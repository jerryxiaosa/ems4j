package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 获取尖电量命令。
 */
@Data
@Accessors(chain = true)
public class GetHigherEnergyCommand implements DeviceCommandRequest {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_HIGHER_ENERGY;
    }
}
