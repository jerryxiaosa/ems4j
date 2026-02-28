package info.zhihui.ems.web.account.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.formatter.AbsoluteMoneyScale2TextFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户销户详情 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountCancelDetailVo", description = "账户销户详情")
public class AccountCancelDetailVo {

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

    @Schema(description = "结算金额展示值（绝对值，保留两位小数）")
    @FormatText(source = "cleanBalanceReal", formatter = AbsoluteMoneyScale2TextFormatter.class)
    private String cleanBalanceAmountText;

    @Schema(description = "操作人")
    private String operatorName;

    @Schema(description = "销户时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "销户表计列表")
    private List<CanceledMeterVo> meterList;
}
