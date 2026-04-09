package info.zhihui.ems.web.report.vo;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.formatter.MoneyScale2TextFormatter;
import info.zhihui.ems.web.common.formatter.PowerScale2TextFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电费报表账户详情。
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricBillReportAccountDetailVo", description = "电费报表账户详情")
public class ElectricBillReportAccountDetailVo {

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系方式")
    private String contactPhone;

    @Schema(description = "电价计费类型编码")
    private Integer electricAccountType;

    @Schema(description = "电价计费类型名称")
    @EnumLabel(source = "electricAccountType", enumClass = ElectricAccountTypeEnum.class)
    private String electricAccountTypeName;

    @Schema(hidden = true)
    private BigDecimal monthlyPayAmount;

    @Schema(description = "包月费用文本")
    @FormatText(source = "monthlyPayAmount", formatter = MoneyScale2TextFormatter.class)
    private String monthlyPayAmountText;

    @Schema(hidden = true)
    private BigDecimal accountBalance;

    @Schema(description = "账户余额文本")
    @FormatText(source = "accountBalance", formatter = MoneyScale2TextFormatter.class)
    private String accountBalanceText;

    @Schema(description = "电表数量")
    private Integer meterCount;

    @Schema(hidden = true)
    private BigDecimal periodConsumePower;

    @Schema(description = "本期电量文本")
    @FormatText(source = "periodConsumePower", formatter = PowerScale2TextFormatter.class)
    private String periodConsumePowerText;

    @Schema(hidden = true)
    private BigDecimal periodElectricChargeAmount;

    @Schema(description = "本期电费文本")
    @FormatText(source = "periodElectricChargeAmount", formatter = MoneyScale2TextFormatter.class)
    private String periodElectricChargeAmountText;

    @Schema(hidden = true)
    private BigDecimal periodRechargeAmount;

    @Schema(description = "本期充值文本")
    @FormatText(source = "periodRechargeAmount", formatter = MoneyScale2TextFormatter.class)
    private String periodRechargeAmountText;

    @Schema(hidden = true)
    private BigDecimal periodCorrectionAmount;

    @Schema(description = "本期补正文本")
    @FormatText(source = "periodCorrectionAmount", formatter = MoneyScale2TextFormatter.class)
    private String periodCorrectionAmountText;

    @Schema(description = "统计日期文本")
    private String dateRangeText;
}
