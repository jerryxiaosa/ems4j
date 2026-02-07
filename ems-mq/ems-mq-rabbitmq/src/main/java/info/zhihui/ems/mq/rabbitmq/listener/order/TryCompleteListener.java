package info.zhihui.ems.mq.rabbitmq.listener.order;

import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.mq.api.message.order.OrderCompleteMessage;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author jerryxiaosa
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TryCompleteListener {
    private final OrderService orderService;

    @RabbitListener(queues = QueueConstant.QUEUE_ORDER_TRY_TO_COMPLETE)
    public void handler(OrderCompleteMessage  message) {
        try {
            orderService.complete(message.getOrderSn());
        } catch (Exception e) {
            // 该链路不依赖消息队列消费重试，失败后由订单扫描任务补偿（OrderPendingCheckTask -> OrderCheckService）。
            log.error("尝试完成订单[{}]失败: {}", message.getOrderSn(), e.getMessage());
        }
    }
}
