package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 获取每日电量方案命令。
 */
@Data
@Accessors(chain = true)
public class GetDailyEnergyPlanCommand implements DeviceCommandRequest {

    private Integer plan;

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN;
    }
}
