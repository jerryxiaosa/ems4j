package info.zhihui.ems.web.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AccountConsumeRecordVo {

    @Schema(description = "记录ID")
    private Integer id;

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "消费编号")
    private String consumeNo;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "开始余额")
    private BigDecimal beginBalance;

    @Schema(description = "结束余额")
    private BigDecimal endBalance;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
