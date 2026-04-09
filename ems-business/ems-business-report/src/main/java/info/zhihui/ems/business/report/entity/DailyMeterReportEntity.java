package info.zhihui.ems.business.report.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("energy_report_daily_meter")
public class DailyMeterReportEntity extends BaseEntity {

    private Integer id;

    private LocalDate reportDate;

    private Integer accountId;

    private Integer ownerId;

    private Integer ownerType;

    private String ownerName;

    private Integer meterId;

    private String meterName;

    private String deviceNo;

    private Integer spaceId;

    private String spaceName;

    private Integer electricAccountType;

    private Integer generateType;

    private BigDecimal beginPower;

    private BigDecimal beginPowerHigher;

    private BigDecimal beginPowerHigh;

    private BigDecimal beginPowerLow;

    private BigDecimal beginPowerLower;

    private BigDecimal beginPowerDeepLow;

    private BigDecimal endPower;

    private BigDecimal endPowerHigher;

    private BigDecimal endPowerHigh;

    private BigDecimal endPowerLow;

    private BigDecimal endPowerLower;

    private BigDecimal endPowerDeepLow;

    private BigDecimal consumePower;

    private BigDecimal consumePowerHigher;

    private BigDecimal consumePowerHigh;

    private BigDecimal consumePowerLow;

    private BigDecimal consumePowerLower;

    private BigDecimal consumePowerDeepLow;

    private BigDecimal electricChargeAmount;

    private BigDecimal electricChargeAmountHigher;

    private BigDecimal electricChargeAmountHigh;

    private BigDecimal electricChargeAmountLow;

    private BigDecimal electricChargeAmountLower;

    private BigDecimal electricChargeAmountDeepLow;

    private BigDecimal displayPriceHigher;

    private BigDecimal displayPriceHigh;

    private BigDecimal displayPriceLow;

    private BigDecimal displayPriceLower;

    private BigDecimal displayPriceDeepLow;

    private BigDecimal correctionPayAmount;

    private BigDecimal correctionRefundAmount;

    private BigDecimal correctionNetAmount;

    private BigDecimal beginBalance;

    private BigDecimal endBalance;

    private BigDecimal rechargeAmount;
}
