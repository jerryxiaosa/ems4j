package info.zhihui.ems.mq.rabbitmq.listener.order;

import info.zhihui.ems.mq.api.message.order.delay.OrderDelayCheckMessage;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author jerryxiaosa
 */
@Component
@Slf4j
public class OverdueListener {
    @RabbitListener(queues = QueueConstant.QUEUE_ORDER_OVERDUE)
    public void handle(OrderDelayCheckMessage message) {
        log.info("处理订单延迟检查消息：{}", message);
    }
}
