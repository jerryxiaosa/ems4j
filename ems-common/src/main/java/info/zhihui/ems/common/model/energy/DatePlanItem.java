package info.zhihui.ems.common.model.energy;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.MonthDay;

/**
 * 日期方案项。
 */
@Data
@Accessors(chain = true)
public class DatePlanItem {

    /**
     * 日期（仅月日）。
     */
    private MonthDay date;

    /**
     * 对应每日电价方案编号。
     */
    private String dailyPlanId;
}
