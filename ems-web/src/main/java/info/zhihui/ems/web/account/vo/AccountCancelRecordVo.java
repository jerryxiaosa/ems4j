package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户销户记录 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountCancelRecordVo", description = "账户销户记录")
public class AccountCancelRecordVo {

    @Schema(description = "销户编号")
    private String cancelNo;

    @Schema(description = "归属名称")
    private String ownerName;

    @Schema(description = "销表数量")
    private Integer electricMeterAmount;

    @Schema(description = "结算类型编码，参考 cleanBalanceType")
    private Integer cleanBalanceType;

    @Schema(description = "结算金额")
    private BigDecimal cleanBalanceReal;

    @Schema(description = "操作人")
    private String operatorName;

    @Schema(description = "销户时间")
    private LocalDateTime cancelTime;

    @Schema(description = "备注")
    private String remark;
}
