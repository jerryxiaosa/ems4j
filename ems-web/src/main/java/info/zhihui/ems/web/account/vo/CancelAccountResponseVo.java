package info.zhihui.ems.web.account.vo;

import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.formatter.AbsoluteMoneyScale2TextFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 销户响应 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "CancelAccountResponseVo", description = "销户结果")
public class CancelAccountResponseVo {

    @Schema(description = "销户编号")
    private String cancelNo;

    @Schema(description = "结算类型编码，参考 cleanBalanceType")
    private Integer cleanBalanceType;

    @Schema(description = "结算金额")
    private BigDecimal amount;

    @Schema(description = "结算金额展示值（绝对值，保留两位小数）")
    @FormatText(source = "amount", formatter = AbsoluteMoneyScale2TextFormatter.class)
    private String cleanBalanceAmountText;
}
