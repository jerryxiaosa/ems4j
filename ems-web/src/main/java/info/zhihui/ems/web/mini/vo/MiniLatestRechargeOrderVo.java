package info.zhihui.ems.web.mini.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 小程序首页最近充值订单视图对象。
 */
@Data
@Accessors(chain = true)
public class MiniLatestRechargeOrderVo {

    private String orderSn;

    private BigDecimal payAmount;

    private String payAmountText;

    private BigDecimal topUpAmount;

    private String topUpAmountText;

    private BigDecimal serviceFeeAmount;

    private String serviceFeeAmountText;

    private String status;

    private String statusName;

    private String createTime;
}
