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

    private Integer dailyPlanId;

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN;
    }

    @Override
    public void validate() {
        if (dailyPlanId == null || dailyPlanId < 1 || dailyPlanId > 2) {
            throw new IllegalArgumentException("日方案编号范围为1~2");
        }
    }
}
