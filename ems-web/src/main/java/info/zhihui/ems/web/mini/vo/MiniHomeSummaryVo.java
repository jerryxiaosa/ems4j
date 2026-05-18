package info.zhihui.ems.web.mini.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 小程序首页摘要视图对象。
 */
@Data
@Accessors(chain = true)
public class MiniHomeSummaryVo {

    private String electricAccountName;

    private Integer electricAccountType;

    private BigDecimal balance;

    private String balanceText;

    private Integer meterCount;

    private BigDecimal lastMonthEnergy;

    private String lastMonthEnergyText;

    private BigDecimal lastMonthFee;

    private String lastMonthFeeText;

    private MiniLatestRechargeOrderVo latestRechargeOrder;
}
