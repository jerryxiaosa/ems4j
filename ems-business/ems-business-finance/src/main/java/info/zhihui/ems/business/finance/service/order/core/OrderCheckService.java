package info.zhihui.ems.business.finance.service.order.core;

/**
 * 订单校验服务接口
 *
 * @author jerryxiaosa
 */
public interface OrderCheckService {

    /**
     * 查找过去7天内状态为待支付（OrderStatusEnum.NOT_PAY）的订单，
     * 并逐一调用 OrderService.complete(orderSn) 尝试完成订单
     */
    void completePendingOrdersInLast7Days();
}
