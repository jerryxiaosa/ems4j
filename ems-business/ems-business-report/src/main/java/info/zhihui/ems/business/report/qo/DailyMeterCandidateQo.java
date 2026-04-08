package info.zhihui.ems.business.report.qo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DailyMeterCandidateQo {

    private Integer accountId;

    private Integer meterId;
}
