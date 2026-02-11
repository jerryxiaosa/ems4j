package info.zhihui.ems.business.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电量消费明细DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class PowerConsumeDetailDto {

    /**
     * 余额消费记录ID
     */
    private Integer id;

    /**
     * 电量消费记录ID
     */
    private Integer meterConsumeRecordId;

    /**
     * 消费编号
     */
    private String consumeNo;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 电表ID
     */
    private Integer meterId;

    /**
     * 电表名称
     */
    private String meterName;

    /**
     * 电表编号
     */
    private String meterNo;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 归属者ID
     */
    private Integer ownerId;

    /**
     * 归属者类型
     */
    private Integer ownerType;

    /**
     * 归属者名称
     */
    private String ownerName;

    /**
     * 开始余额
     */
    private BigDecimal beginBalance;

    /**
     * 消费总金额
     */
    private BigDecimal consumeAmount;

    /**
     * 尖金额
     */
    private BigDecimal consumeAmountHigher;

    /**
     * 峰金额
     */
    private BigDecimal consumeAmountHigh;

    /**
     * 平金额
     */
    private BigDecimal consumeAmountLow;

    /**
     * 谷金额
     */
    private BigDecimal consumeAmountLower;

    /**
     * 深谷金额
     */
    private BigDecimal consumeAmountDeepLow;

    /**
     * 结束余额
     */
    private BigDecimal endBalance;

    /**
     * 阶梯起始值
     */
    private BigDecimal stepStartValue;

    /**
     * 历史电量偏移
     */
    private BigDecimal historyPowerOffset;

    /**
     * 阶梯倍率
     */
    private BigDecimal stepRate;

    /**
     * 尖单价
     */
    private BigDecimal priceHigher;

    /**
     * 峰单价
     */
    private BigDecimal priceHigh;

    /**
     * 平单价
     */
    private BigDecimal priceLow;

    /**
     * 谷单价
     */
    private BigDecimal priceLower;

    /**
     * 深谷单价
     */
    private BigDecimal priceDeepLow;

    /**
     * 开始总电量
     */
    private BigDecimal beginPower;

    /**
     * 结束总电量
     */
    private BigDecimal endPower;

    /**
     * 消费总电量
     */
    private BigDecimal consumePower;

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
     * 起始记录时间
     */
    private LocalDateTime beginRecordTime;

    /**
     * 结束记录时间
     */
    private LocalDateTime endRecordTime;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;
}
