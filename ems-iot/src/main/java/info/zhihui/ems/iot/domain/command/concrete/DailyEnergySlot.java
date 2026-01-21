package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

/**
 * 每日电量方案时段。
 */
@Data
@Accessors(chain = true)
public class DailyEnergySlot {

    private ElectricPricePeriodEnum period;
    private LocalTime time;
}
