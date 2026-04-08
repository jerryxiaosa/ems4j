package info.zhihui.ems.business.report.qo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 当前电表快照
 */
@Data
@Accessors(chain = true)
public class MeterSnapshotSourceQo {

    private Integer accountId;

    private Integer meterId;

    private String meterName;

    private String deviceNo;

    private Integer spaceId;

    private String spaceName;
}
