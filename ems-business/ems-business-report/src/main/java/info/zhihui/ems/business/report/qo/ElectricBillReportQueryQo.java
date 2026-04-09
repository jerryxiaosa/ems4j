package info.zhihui.ems.business.report.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 电费报表查询对象。
 */
@Data
@Accessors(chain = true)
public class ElectricBillReportQueryQo {

    private String accountNameLike;

    private LocalDate startDate;

    private LocalDate endDate;
}
