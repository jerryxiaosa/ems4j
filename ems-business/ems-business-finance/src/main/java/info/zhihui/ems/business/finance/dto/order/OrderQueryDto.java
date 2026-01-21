package info.zhihui.ems.business.finance.dto.order;

import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 订单查询参数对象
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class OrderQueryDto {

    /**
     * 订单状态
     */
    private OrderStatusEnum orderStatus;

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
    private PaymentChannelEnum paymentChannel;

    /**
     * 用户ID
     */
    private Integer userId;
}