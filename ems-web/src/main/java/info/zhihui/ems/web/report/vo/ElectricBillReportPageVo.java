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
 * 电费报表列表项。
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricBillReportPageVo", description = "电费报表列表项")
public class ElectricBillReportPageVo {

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "电价计费类型编码")
    private Integer electricAccountType;

    @Schema(description = "电价计费类型名称")
    @EnumLabel(source = "electricAccountType", enumClass = ElectricAccountTypeEnum.class)
    private String electricAccountTypeName;

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

    @Schema(description = "充值金额文本")
    @FormatText(source = "periodRechargeAmount", formatter = MoneyScale2TextFormatter.class)
    private String periodRechargeAmountText;

    @Schema(hidden = true)
    private BigDecimal periodCorrectionAmount;

    @Schema(description = "补正金额文本")
    @FormatText(source = "periodCorrectionAmount", formatter = MoneyScale2TextFormatter.class)
    private String periodCorrectionAmountText;

    @Schema(hidden = true)
    private BigDecimal totalDebitAmount;

    @Schema(description = "合计费用文本")
    @FormatText(source = "totalDebitAmount", formatter = MoneyScale2TextFormatter.class)
    private String totalDebitAmountText;
}
