package info.zhihui.ems.business.finance.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电量消费记录查询对象QO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricPowerConsumeRecordQo {

    /**
     * 表名称模糊匹配（可选）
     */
    private String meterNameLike;

    /**
     * 房间/空间名称模糊匹配（可选）
     */
    private String spaceNameLike;

    /**
     * 消费时间范围开始时间（可选）
     */
    private LocalDateTime beginTime;

    /**
     * 消费时间范围结束时间（可选）
     */
    private LocalDateTime endTime;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 电表ID
     */
    private Integer meterId;

    /**
     * 消费类型
     */
    private Integer consumeType;
}
