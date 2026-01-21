package info.zhihui.ems.business.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 水电表阶梯起始数据
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("energy_account_meter_step")
public class MeterStepEntity extends BaseEntity {

    private Integer id;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 表类型，0:电表,1:水表
     */
    private Integer meterType;

    /**
     * 表id
     */
    private Integer meterId;

    /**
     * 年度阶梯起始值，受换表影响
     */
    private BigDecimal stepStartValue;

    /**
     * 继承的历史阶梯用量补偿
     */
    private BigDecimal historyPowerOffset;

    /**
     * 年度
     */
    private Integer currentYear;

    /**
     * 是否最新记录
     */
    private Boolean isLatest;

}
