package info.zhihui.ems.business.finance.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class BalanceQo {
    private Integer balanceRelationId;
    private Integer balanceType;
    private Integer accountId;
    private BigDecimal amount;
}
