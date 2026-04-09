package info.zhihui.ems.web.report.vo;

import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.formatter.MoneyScale2TextFormatter;
import info.zhihui.ems.web.common.formatter.PowerScale2TextFormatter;
import info.zhihui.ems.web.common.formatter.PriceScale4TextFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电费报表电表详情。
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricBillReportMeterDetailVo", description = "电费报表电表详情")
public class ElectricBillReportMeterDetailVo {

    @Schema(description = "电表ID")
    private Integer meterId;

    @Schema(description = "电表编号")
    private String deviceNo;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(hidden = true)
    private BigDecimal consumePowerHigher;

    @Schema(description = "尖电量文本")
    @FormatText(source = "consumePowerHigher", formatter = PowerScale2TextFormatter.class)
    private String consumePowerHigherText;

    @Schema(hidden = true)
    private BigDecimal consumePowerHigh;

    @Schema(description = "峰电量文本")
    @FormatText(source = "consumePowerHigh", formatter = PowerScale2TextFormatter.class)
    private String consumePowerHighText;

    @Schema(hidden = true)
    private BigDecimal consumePowerLow;

    @Schema(description = "平电量文本")
    @FormatText(source = "consumePowerLow", formatter = PowerScale2TextFormatter.class)
    private String consumePowerLowText;

    @Schema(hidden = true)
    private BigDecimal consumePowerLower;

    @Schema(description = "谷电量文本")
    @FormatText(source = "consumePowerLower", formatter = PowerScale2TextFormatter.class)
    private String consumePowerLowerText;

    @Schema(hidden = true)
    private BigDecimal consumePowerDeepLow;

    @Schema(description = "深谷电量文本")
    @FormatText(source = "consumePowerDeepLow", formatter = PowerScale2TextFormatter.class)
    private String consumePowerDeepLowText;

    @Schema(hidden = true)
    private BigDecimal displayPriceHigher;

    @Schema(description = "尖单价文本")
    @FormatText(source = "displayPriceHigher", formatter = PriceScale4TextFormatter.class)
    private String displayPriceHigherText;

    @Schema(hidden = true)
    private BigDecimal displayPriceHigh;

    @Schema(description = "峰单价文本")
    @FormatText(source = "displayPriceHigh", formatter = PriceScale4TextFormatter.class)
    private String displayPriceHighText;

    @Schema(hidden = true)
    private BigDecimal displayPriceLow;

    @Schema(description = "平单价文本")
    @FormatText(source = "displayPriceLow", formatter = PriceScale4TextFormatter.class)
    private String displayPriceLowText;

    @Schema(hidden = true)
    private BigDecimal displayPriceLower;

    @Schema(description = "谷单价文本")
    @FormatText(source = "displayPriceLower", formatter = PriceScale4TextFormatter.class)
    private String displayPriceLowerText;

    @Schema(hidden = true)
    private BigDecimal displayPriceDeepLow;

    @Schema(description = "深谷单价文本")
    @FormatText(source = "displayPriceDeepLow", formatter = PriceScale4TextFormatter.class)
    private String displayPriceDeepLowText;

    @Schema(hidden = true)
    private BigDecimal electricChargeAmountHigher;

    @Schema(description = "尖电费文本")
    @FormatText(source = "electricChargeAmountHigher", formatter = MoneyScale2TextFormatter.class)
    private String electricChargeAmountHigherText;

    @Schema(hidden = true)
    private BigDecimal electricChargeAmountHigh;

    @Schema(description = "峰电费文本")
    @FormatText(source = "electricChargeAmountHigh", formatter = MoneyScale2TextFormatter.class)
    private String electricChargeAmountHighText;

    @Schema(hidden = true)
    private BigDecimal electricChargeAmountLow;

    @Schema(description = "平电费文本")
    @FormatText(source = "electricChargeAmountLow", formatter = MoneyScale2TextFormatter.class)
    private String electricChargeAmountLowText;

    @Schema(hidden = true)
    private BigDecimal electricChargeAmountLower;

    @Schema(description = "谷电费文本")
    @FormatText(source = "electricChargeAmountLower", formatter = MoneyScale2TextFormatter.class)
    private String electricChargeAmountLowerText;

    @Schema(hidden = true)
    private BigDecimal electricChargeAmountDeepLow;

    @Schema(description = "深谷电费文本")
    @FormatText(source = "electricChargeAmountDeepLow", formatter = MoneyScale2TextFormatter.class)
    private String electricChargeAmountDeepLowText;

    @Schema(hidden = true)
    private BigDecimal totalConsumePower;

    @Schema(description = "总电量文本")
    @FormatText(source = "totalConsumePower", formatter = PowerScale2TextFormatter.class)
    private String totalConsumePowerText;

    @Schema(hidden = true)
    private BigDecimal totalElectricChargeAmount;

    @Schema(description = "总电费文本")
    @FormatText(source = "totalElectricChargeAmount", formatter = MoneyScale2TextFormatter.class)
    private String totalElectricChargeAmountText;

    @Schema(hidden = true)
    private BigDecimal totalRechargeAmount;

    @Schema(description = "总充值文本")
    @FormatText(source = "totalRechargeAmount", formatter = MoneyScale2TextFormatter.class)
    private String totalRechargeAmountText;

    @Schema(hidden = true)
    private BigDecimal totalCorrectionAmount;

    @Schema(description = "总补正文本")
    @FormatText(source = "totalCorrectionAmount", formatter = MoneyScale2TextFormatter.class)
    private String totalCorrectionAmountText;
}
