package info.zhihui.ems.business.report.qo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 电费报表电表数量查询结果。
 */
@Data
@Accessors(chain = true)
public class ElectricBillMeterCountQo {

    private Integer accountId;

    private Integer meterCount;
}
