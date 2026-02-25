package info.zhihui.ems.web.account.vo;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.components.translate.annotation.BizLabel;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.formatter.MoneyScale2TextFormatter;
import info.zhihui.ems.web.account.resolver.WarnPlanNameResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户详情 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountDetailVo", description = "账户详情信息")
public class AccountDetailVo {

    @Schema(description = "账户ID")
    private Integer id;

    @Schema(description = "账户类型，参考 ownerType")
    private Integer ownerType;

    @Schema(description = "账户类型名称")
    @EnumLabel(source = "ownerType", enumClass = OwnerTypeEnum.class)
    private String ownerTypeName;

    @Schema(description = "账户归属者ID")
    private Integer ownerId;

    @Schema(description = "账户归属者名称")
    private String ownerName;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系方式")
    private String contactPhone;

    @Schema(description = "电费计费类型，参考 electricAccountType")
    private Integer electricAccountType;

    @Schema(description = "电费计费类型名称")
    @EnumLabel(source = "electricAccountType", enumClass = ElectricAccountTypeEnum.class)
    private String electricAccountTypeName;

    @Schema(description = "包月费用")
    private BigDecimal monthlyPayAmount;

    @Schema(description = "电费余额")
    private BigDecimal electricBalanceAmount;

    @Schema(description = "电费余额展示值（保留两位小数）")
    @FormatText(source = "electricBalanceAmount", formatter = MoneyScale2TextFormatter.class)
    private String electricBalanceAmountText;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;

    @Schema(description = "预警方案名称")
    @BizLabel(source = "warnPlanId", resolver = WarnPlanNameResolver.class)
    private String warnPlanName;

    @Schema(description = "电费预警级别，参考 warnType")
    private String electricWarnType;

    @Schema(description = "电费预警级别名称")
    @EnumLabel(source = "electricWarnType", enumClass = WarnTypeEnum.class)
    private String electricWarnTypeName;

    @Schema(description = "账户已开户电表数量")
    private Integer openedMeterCount;

    @Schema(description = "账户可开户电表总数")
    private Integer totalOpenableMeterCount;

    @Schema(description = "账户所属电表列表")
    private List<AccountMeterVo> meterList;
}
