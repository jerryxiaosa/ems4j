package info.zhihui.ems.web.account.vo;

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

    @Schema(description = "账户归属者ID")
    private Integer ownerId;

    @Schema(description = "账户归属者名称")
    private String ownerName;

    @Schema(description = "电费计费类型，参考 electricAccountType")
    private Integer electricAccountType;

    @Schema(description = "包月费用")
    private BigDecimal monthlyPayAmount;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;

    @Schema(description = "电费预警级别，参考 warnType")
    private String electricWarnType;

    @Schema(description = "账户所属电表列表")
    private List<AccountMeterVo> meterList;
}

