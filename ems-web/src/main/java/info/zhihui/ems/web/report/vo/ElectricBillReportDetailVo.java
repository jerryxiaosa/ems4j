package info.zhihui.ems.web.report.vo;

import info.zhihui.ems.components.translate.annotation.TranslateChild;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 电费报表详情。
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricBillReportDetailVo", description = "电费报表详情")
public class ElectricBillReportDetailVo {

    @Schema(description = "账户信息")
    @TranslateChild
    private ElectricBillReportAccountDetailVo accountInfo;

    @Schema(description = "电表信息列表")
    @TranslateChild
    private List<ElectricBillReportMeterDetailVo> meterList;
}
