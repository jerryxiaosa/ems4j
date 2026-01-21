package info.zhihui.ems.business.finance.service.order.handler.completion;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.common.model.MqMessage;

/**
 * @author jerryxiaosa
 */
public interface OrderCompletionHandler {

    /**
     * 获取订单类型
     *
     * @return 订单类型
     */
    OrderTypeEnum getOrderType();

    /**
     * 订单成功后创建MQ消息
     *
     * @param orderBo 订单信息
     * @return MQ消息
     */
    MqMessage createMessageAfterOrderSuccess(OrderBo orderBo);
}
