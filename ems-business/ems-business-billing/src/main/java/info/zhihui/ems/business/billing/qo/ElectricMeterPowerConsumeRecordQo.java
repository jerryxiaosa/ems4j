package info.zhihui.ems.business.billing.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电表电量消费记录查询对象
 */
@Data
@Accessors(chain = true)
public class ElectricMeterPowerConsumeRecordQo {

    /**
     * 电表ID
     */
    private Integer meterId;

    /**
     * 消费时间范围开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 消费时间范围结束时间
     */
    private LocalDateTime endTime;

    /**
     * 限制条数
     */
    private Integer limit;
}
