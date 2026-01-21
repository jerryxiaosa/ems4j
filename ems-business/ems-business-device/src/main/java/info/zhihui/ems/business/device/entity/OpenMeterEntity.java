package info.zhihui.ems.business.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("energy_open_meter_record")
public class OpenMeterEntity extends BaseEntity {

    private Integer id;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 电表、水表
     */
    private Integer meterType;

    /**
     * 表id
     */
    private Integer meterId;

    /**
     * 电量读数，总用量
     */
    private BigDecimal power;

    private BigDecimal powerHigher;

    private BigDecimal powerHigh;

    private BigDecimal powerLow;

    private BigDecimal powerLower;

    private BigDecimal powerDeepLow;

    /**
     * 读表时间
     */
    private LocalDateTime showTime;
}
