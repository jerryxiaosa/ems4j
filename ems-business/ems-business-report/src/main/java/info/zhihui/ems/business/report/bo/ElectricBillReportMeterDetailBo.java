package info.zhihui.ems.business.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电费报表电表详情。
 */
@Data
@Accessors(chain = true)
public class ElectricBillReportMeterDetailBo {

    /**
     * 电表ID。
     */
    private Integer meterId;

    /**
     * 电表编号。
     */
    private String deviceNo;

    /**
     * 电表名称。
     */
    private String meterName;

    /**
     * 尖电量。
     */
    private BigDecimal consumePowerHigher;

    /**
     * 峰电量。
     */
    private BigDecimal consumePowerHigh;

    /**
     * 平电量。
     */
    private BigDecimal consumePowerLow;

    /**
     * 谷电量。
     */
    private BigDecimal consumePowerLower;

    /**
     * 深谷电量。
     */
    private BigDecimal consumePowerDeepLow;

    /**
     * 尖单价。
     */
    private BigDecimal displayPriceHigher;

    /**
     * 峰单价。
     */
    private BigDecimal displayPriceHigh;

    /**
     * 平单价。
     */
    private BigDecimal displayPriceLow;

    /**
     * 谷单价。
     */
    private BigDecimal displayPriceLower;

    /**
     * 深谷单价。
     */
    private BigDecimal displayPriceDeepLow;

    /**
     * 尖电费。
     */
    private BigDecimal electricChargeAmountHigher;

    /**
     * 峰电费。
     */
    private BigDecimal electricChargeAmountHigh;

    /**
     * 平电费。
     */
    private BigDecimal electricChargeAmountLow;

    /**
     * 谷电费。
     */
    private BigDecimal electricChargeAmountLower;

    /**
     * 深谷电费。
     */
    private BigDecimal electricChargeAmountDeepLow;

    /**
     * 总电量。
     */
    private BigDecimal totalConsumePower;

    /**
     * 总电费。
     */
    private BigDecimal totalElectricChargeAmount;

    /**
     * 总充值。
     */
    private BigDecimal totalRechargeAmount;

    /**
     * 总补正。
     */
    private BigDecimal totalCorrectionAmount;
}
