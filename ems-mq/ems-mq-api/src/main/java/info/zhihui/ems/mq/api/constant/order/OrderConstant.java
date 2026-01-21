package info.zhihui.ems.mq.api.constant.order;

/**
 * @author jerryxiaosa
 */
public final class OrderConstant {
    private OrderConstant() {
    }

    public static final String ORDER_DELAY_DESTINATION = "order-delay.exchange";
    public static final String ROUTING_DELAYED_ORDER_CHECK = "routing.delay.order.check";

    public static final String ORDER_DESTINATION = "order.exchange";
    // 尝试完成订单
    public static final String ROUTING_KEY_ORDER_TRY_COMPLETE = "routing.order.tryComplete";

    // 订单状态变更
    public static final String ROUTING_KEY_ORDER_STATUS = "routing.order.status.#";
    // ROUTING_KEY_ORDER_STATUS_SUCCESS扩展
    // 订单成功，能耗充值成功
    public static final String ROUTING_KEY_ORDER_STATUS_SUCCESS_ENERGY_TOP_UP = "routing.order.status.success.energyTopUp";
    // 订单成功，销户成功
    public static final String ROUTING_KEY_ORDER_STATUS_SUCCESS_TERMINATION = "routing.order.status.success.termination";
}
