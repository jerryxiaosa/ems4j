package info.zhihui.ems.iot.domain.command.concrete;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 日期方案项。
 */
@Data
@Accessors(chain = true)
public class DatePlanItem {

    private Integer month;
    private Integer day;
    private Integer plan;
}
