package info.zhihui.ems.business.report.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class RechargeSourceItemQo {

    private String orderSn;

    private Integer accountId;

    private Integer meterId;

    private Integer balanceType;

    private BigDecimal amount;

    private BigDecimal serviceAmount;

    private BigDecimal beginBalance;

    private BigDecimal endBalance;

    private LocalDateTime createTime;
}
