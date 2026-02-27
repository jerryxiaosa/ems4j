package info.zhihui.ems.web.owner.vo;

import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.formatter.MoneyScale2TextFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 主体账户状态
 */
@Data
@Accessors(chain = true)
@Schema(name = "OwnerAccountStatusVo", description = "主体账户状态")
public class OwnerAccountStatusVo {

    @Schema(description = "主体类型（0企业，1个人）")
    private Integer ownerType;

    @Schema(description = "主体ID")
    private Integer ownerId;

    @Schema(description = "是否已开户")
    private Boolean hasAccount;

    @Schema(description = "账户ID，未开户时为空")
    private Integer accountId;

    @Schema(description = "电费计费类型，未开户时为空")
    private Integer electricAccountType;

    @Schema(description = "电价方案ID，未开户时为空")
    private Integer electricPricePlanId;

    @Schema(description = "费用预警方案ID，未开户时为空")
    private Integer warnPlanId;

    @Schema(description = "包月金额，未开户时为空")
    private BigDecimal monthlyPayAmount;

    @Schema(description = "包月金额展示值（保留两位小数）")
    @FormatText(source = "monthlyPayAmount", formatter = MoneyScale2TextFormatter.class)
    private String monthlyPayAmountText;
}
