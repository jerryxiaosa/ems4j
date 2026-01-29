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
     * 对应每日电价方案编号。
     */
    private String dailyPlanId;
}
