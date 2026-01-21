package info.zhihui.ems.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电表电量消费记录实体类
 * 对应数据库表：energy_electric_meter_power_consume_record
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@TableName("energy_electric_meter_power_consume_record")
public class ElectricMeterPowerConsumeRecordEntity {
    private Integer id;

    /**
     * 电表ID
     */
    private Integer meterId;

    /**
     * 是否计算
     */
    private Boolean isCalculate;

    /**
     * 计算类型
     */
    private Integer calculateType;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 空间ID
     */
    private Integer spaceId;

    /**
     * 开始记录ID
     */
    private Integer beginRecordId;

    /**
     * 开始总电量
     */
    private BigDecimal beginPower;

    /**
     * 开始电量（尖）
     */
    private BigDecimal beginPowerHigher;

    /**
     * 开始电量（峰）
     */
    private BigDecimal beginPowerHigh;

    /**
     * 开始电量（平）
     */
    private BigDecimal beginPowerLow;

    /**
     * 开始电量（谷）
     */
    private BigDecimal beginPowerLower;

    /**
     * 开始电量（深谷）
     */
    private BigDecimal beginPowerDeepLow;

    /**
     * 开始记录时间
     */
    private LocalDateTime beginRecordTime;

    /**
     * 结束记录ID
     */
    private Integer endRecordId;

    /**
     * 结束电量
     */
    private BigDecimal endPower;

    /**
     * 结束电量（尖）
     */
    private BigDecimal endPowerHigher;

    /**
     * 结束电量（峰）
     */
    private BigDecimal endPowerHigh;

    /**
     * 结束电量（平）
     */
    private BigDecimal endPowerLow;

    /**
     * 结束电量（谷）
     */
    private BigDecimal endPowerLower;

    /**
     * 结束电量（深谷）
     */
    private BigDecimal endPowerDeepLow;

    /**
     * 结束记录时间
     */
    private LocalDateTime endRecordTime;

    /**
     * 消费总电量
     */
    private BigDecimal consumePower;

    /**
     * 消费电量（尖）
     */
    private BigDecimal consumePowerHigher;

    /**
     * 消费电量（峰）
     */
    private BigDecimal consumePowerHigh;

    /**
     * 消费电量（平）
     */
    private BigDecimal consumePowerLow;

    /**
     * 消费电量（谷）
     */
    private BigDecimal consumePowerLower;

    /**
     * 消费电量（深谷）
     */
    private BigDecimal consumePowerDeepLow;

    /**
     * 电表消费时间
     */
    private LocalDateTime meterConsumeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
}