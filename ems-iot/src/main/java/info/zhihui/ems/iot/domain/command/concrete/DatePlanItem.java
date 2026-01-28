package info.zhihui.ems.iot.domain.command.concrete;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 日期方案项。
 */
@Data
@Accessors(chain = true)
public class DatePlanItem {

    private String month;
    private String day;

    /**
     * 这里的plan指的是用哪个DailyEnergySlot
     */
    private String plan;
}
