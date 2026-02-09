package info.zhihui.ems.common.model.energy;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.zhihui.ems.common.utils.jackson.MonthDayDashFormatDeserializer;
import info.zhihui.ems.common.utils.jackson.MonthDayDashFormatSerializer;
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
    @JsonSerialize(using = MonthDayDashFormatSerializer.class)
    @JsonDeserialize(using = MonthDayDashFormatDeserializer.class)
    private MonthDay date;

    /**
     * 对应每日电价方案编号。
     */
    private String dailyPlanId;
}
