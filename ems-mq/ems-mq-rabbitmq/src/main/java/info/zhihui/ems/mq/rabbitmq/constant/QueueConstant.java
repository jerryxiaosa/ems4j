package info.zhihui.ems.mq.rabbitmq.constant;

/**
 * @author jerryxiaosa
 */
public class QueueConstant {
    public static final String QUEUE_ORDER_OVERDUE = "queue.order.overdue";

    public static final String QUEUE_ORDER_TRY_TO_COMPLETE = "queue.order.tryToComplete";

    public static final String QUEUE_ORDER_STATUS_CHANGE = "queue.order.status.change";

    public static final String QUEUE_ORDER_SUCCESS_ENERGY_TOP_UP = "queue.order.success.energyTopUp";

    public static final String QUEUE_ORDER_SUCCESS_TERMINATION = "queue.order.success.termination";

    public static final String QUEUE_FINANCE_BALANCE_CHANGED = "queue.finance.balanceChanged";
}
