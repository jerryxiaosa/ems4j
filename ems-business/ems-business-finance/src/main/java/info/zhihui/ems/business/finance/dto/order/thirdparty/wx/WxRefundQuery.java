package info.zhihui.ems.business.finance.dto.order.thirdparty.wx;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class WxRefundQuery {
    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 退款编号
     */
    private String refundSn;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason;

}
