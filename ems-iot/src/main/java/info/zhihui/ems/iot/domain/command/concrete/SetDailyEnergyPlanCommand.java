package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;
import java.util.List;

/**
 * 设置每日电量方案命令。
 */
@Data
@Accessors(chain = true)
public class SetDailyEnergyPlanCommand implements DeviceCommandRequest {

    private static final int MAX_SLOT_COUNT = 14;

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
        if (dailyPlanId < 1 || dailyPlanId > 2) {
            throw new IllegalArgumentException("日方案编号范围为1~2");
        }
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("时段配置不能为空");
        }
        if (slots.size() > MAX_SLOT_COUNT) {
            throw new IllegalArgumentException("时段配置最多支持14组");
        }

        LocalTime previousTime = null;
        for (DailyEnergySlot slot : slots) {
            if (slot == null || slot.getPeriod() == null || slot.getTime() == null) {
                throw new IllegalArgumentException("时段配置不完整");
            }
            validatePeriod(slot.getPeriod());
            LocalTime currentTime = slot.getTime();
            if (previousTime != null && !currentTime.isAfter(previousTime)) {
                throw new IllegalArgumentException("时段时间必须升序且不能重复");
            }
            previousTime = currentTime;
        }
    }

    private void validatePeriod(ElectricPricePeriodEnum period) {
        if (ElectricPricePeriodEnum.TOTAL.equals(period)) {
            throw new IllegalArgumentException("时段费率不支持总电价");
        }
        Integer code = period.getCode();
        if (code == null || code < ElectricPricePeriodEnum.HIGHER.getCode()
                || code > ElectricPricePeriodEnum.DEEP_LOW.getCode()) {
            throw new IllegalArgumentException("时段费率不正确");
        }
    }
}
