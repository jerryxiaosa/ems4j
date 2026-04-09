package info.zhihui.ems.web.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 电费报表查询参数。
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricBillReportQueryVo", description = "电费报表查询参数")
public class ElectricBillReportQueryVo {

    @Schema(description = "账户名称模糊匹配")
    private String accountNameLike;

    @NotNull
    @Schema(description = "统计开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull
    @Schema(description = "统计结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;
}
