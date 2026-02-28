package info.zhihui.ems.web.account.vo;

import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.formatter.MoneyScale2TextFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户下电表信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountMeterVo", description = "账户所属电表信息")
public class AccountMeterVo {

    @Schema(description = "电表ID")
    private Integer id;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "电表编号")
    private String meterNo;

    @Schema(description = "空间ID")
    private Integer spaceId;

    @Schema(description = "计费方案ID")
    private Integer pricePlanId;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;

    @Schema(description = "预警类型")
    private String warnType;

    @Schema(description = "预警等级")
    @EnumLabel(source = "warnType", enumClass = WarnTypeEnum.class)
    private String warnTypeName;

    @Schema(description = "CT变比")
    private Integer ct;

    @Schema(description = "电表余额")
    private BigDecimal meterBalanceAmount;

    @Schema(description = "电表余额展示值（保留两位小数）")
    @FormatText(source = "meterBalanceAmount", formatter = MoneyScale2TextFormatter.class)
    private String meterBalanceAmountText;

    @Schema(description = "所在位置")
    private String spaceName;

    @Schema(description = "所属区域名称列表")
    private List<String> spaceParentNames;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "离线时长")
    private String offlineDurationText;
}
