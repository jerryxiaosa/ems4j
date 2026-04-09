package info.zhihui.ems.business.report.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电表原始上报与关系快照的联合视图。
 */
@Data
@Accessors(chain = true)
public class PowerRecordSnapshotSourceQo {

    private Integer recordId;

    private Integer accountId;

    private Integer meterId;

    private String meterName;

    private String deviceNo;

    private LocalDateTime recordTime;

    private Integer ownerId;

    private Integer ownerType;

    private String ownerName;

    private Integer spaceId;

    private String spaceName;

    private Integer electricAccountType;
}
