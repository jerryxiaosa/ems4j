package info.zhihui.ems.business.report.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 电费报表查询条件。
 */
@Data
@Accessors(chain = true)
public class ElectricBillReportQueryDto {

    private String accountNameLike;

    private LocalDate startDate;

    private LocalDate endDate;
}
