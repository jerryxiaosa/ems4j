package info.zhihui.ems.business.finance.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 订单查询对象
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class OrderQueryQo {

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 订单创建开始时间
     */
    private LocalDateTime createStartTime;

    /**
     * 订单创建结束时间
     */
    private LocalDateTime createEndTime;

    /**
     * 支付渠道
     */
    private String paymentChannel;

    /**
     * 用户ID
     */
    private Integer userId;
}
