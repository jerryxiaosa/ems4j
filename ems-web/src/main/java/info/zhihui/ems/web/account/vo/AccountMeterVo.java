package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

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

    @Schema(description = "是否在线")
    private Boolean isOnline;
}
