package info.zhihui.ems.business.account.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户候选电表数据传输对象
 */
@Data
@Accessors(chain = true)
public class AccountCandidateMeterDto {

    /**
     * 电表ID
     */
    private Integer id;

    /**
     * 电表名称
     */
    private String meterName;

    /**
     * 电表编号
     */
    private String meterNo;

    /**
     * 空间ID
     */
    private Integer spaceId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 所属区域名称列表
     */
    private List<String> spaceParentNames;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 最近一次确认在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 是否预付费
     */
    private Boolean isPrepay;
}
