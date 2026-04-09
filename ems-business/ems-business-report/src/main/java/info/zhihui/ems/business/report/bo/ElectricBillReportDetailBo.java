package info.zhihui.ems.business.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 电费报表详情。
 */
@Data
@Accessors(chain = true)
public class ElectricBillReportDetailBo {

    /**
     * 账户汇总信息。
     */
    private ElectricBillReportAccountDetailBo accountInfo;

    /**
     * 电表汇总列表。
     */
    private List<ElectricBillReportMeterDetailBo> meterList;
}
