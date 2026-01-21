package info.zhihui.ems.mq.rabbitmq.listener.order;

import info.zhihui.ems.mq.api.message.order.status.OrderChangeStatusMessage;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author jerryxiaosa
 */
@Component
@Slf4j
public class NotSuccessChangeListener {
    @RabbitListener(queues = QueueConstant.QUEUE_ORDER_STATUS_CHANGE)
    public void handle(OrderChangeStatusMessage message) {
        log.info("订单[{}]状态变更为：{}", message.getOrderSn(), message.getOrderStatus());
    }
}
