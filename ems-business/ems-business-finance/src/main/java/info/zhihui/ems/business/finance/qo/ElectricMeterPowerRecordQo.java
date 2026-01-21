package info.zhihui.ems.business.finance.qo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterPowerRecordQo {
    private Integer meterId;
    private Integer accountId;
    private Integer limit = 1;
}
