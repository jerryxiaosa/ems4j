package info.zhihui.ems.web.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电量消费明细出参
 */
@Data
@Accessors(chain = true)
public class PowerConsumeDetailVo {

    @Schema(description = "余额消费记录ID")
    private Integer id;

    @Schema(description = "电量消费记录ID")
    private Integer meterConsumeRecordId;

    @Schema(description = "消费编号")
    private String consumeNo;

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "电表ID")
    private Integer meterId;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "电表编号")
    private String deviceNo;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "归属者ID")
    private Integer ownerId;

    @Schema(description = "归属者类型")
    private Integer ownerType;

    @Schema(description = "归属者名称")
    private String ownerName;

    @Schema(description = "开始余额")
    private BigDecimal beginBalance;

    @Schema(description = "消费总金额")
    private BigDecimal consumeAmount;

    @Schema(description = "尖金额")
    private BigDecimal consumeAmountHigher;

    @Schema(description = "峰金额")
    private BigDecimal consumeAmountHigh;

    @Schema(description = "平金额")
    private BigDecimal consumeAmountLow;

    @Schema(description = "谷金额")
    private BigDecimal consumeAmountLower;

    @Schema(description = "深谷金额")
    private BigDecimal consumeAmountDeepLow;

    @Schema(description = "结束余额")
    private BigDecimal endBalance;

    @Schema(description = "阶梯起始值")
    private BigDecimal stepStartValue;

    @Schema(description = "历史电量偏移")
    private BigDecimal historyPowerOffset;

    @Schema(description = "阶梯倍率")
    private BigDecimal stepRate;

    @Schema(description = "尖单价")
    private BigDecimal priceHigher;

    @Schema(description = "峰单价")
    private BigDecimal priceHigh;

    @Schema(description = "平单价")
    private BigDecimal priceLow;

    @Schema(description = "谷单价")
    private BigDecimal priceLower;

    @Schema(description = "深谷单价")
    private BigDecimal priceDeepLow;

    @Schema(description = "开始总电量")
    private BigDecimal beginPower;

    @Schema(description = "结束总电量")
    private BigDecimal endPower;

    @Schema(description = "消费总电量")
    private BigDecimal consumePower;

    @Schema(description = "开始电量（尖）")
    private BigDecimal beginPowerHigher;

    @Schema(description = "开始电量（峰）")
    private BigDecimal beginPowerHigh;

    @Schema(description = "开始电量（平）")
    private BigDecimal beginPowerLow;

    @Schema(description = "开始电量（谷）")
    private BigDecimal beginPowerLower;

    @Schema(description = "开始电量（深谷）")
    private BigDecimal beginPowerDeepLow;

    @Schema(description = "结束电量（尖）")
    private BigDecimal endPowerHigher;

    @Schema(description = "结束电量（峰）")
    private BigDecimal endPowerHigh;

    @Schema(description = "结束电量（平）")
    private BigDecimal endPowerLow;

    @Schema(description = "结束电量（谷）")
    private BigDecimal endPowerLower;

    @Schema(description = "结束电量（深谷）")
    private BigDecimal endPowerDeepLow;

    @Schema(description = "消费电量（尖）")
    private BigDecimal consumePowerHigher;

    @Schema(description = "消费电量（峰）")
    private BigDecimal consumePowerHigh;

    @Schema(description = "消费电量（平）")
    private BigDecimal consumePowerLow;

    @Schema(description = "消费电量（谷）")
    private BigDecimal consumePowerLower;

    @Schema(description = "消费电量（深谷）")
    private BigDecimal consumePowerDeepLow;

    @Schema(description = "起始记录时间")
    private LocalDateTime beginRecordTime;

    @Schema(description = "结束记录时间")
    private LocalDateTime endRecordTime;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;
}
