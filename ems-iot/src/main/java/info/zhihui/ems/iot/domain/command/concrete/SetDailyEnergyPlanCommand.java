package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;

/**
 * 设置每日电量方案命令。
 */
@Data
@Accessors(chain = true)
public class SetDailyEnergyPlanCommand implements DeviceCommandRequest {

    private Integer dailyPlanId;
    private List<DailyEnergySlot> slots;

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN;
    }

    @Override
    public void validate() {
        if (dailyPlanId == null) {
            throw new IllegalArgumentException("日方案编号不能为空");
        }
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("时段配置不能为空");
        }
    }
}
