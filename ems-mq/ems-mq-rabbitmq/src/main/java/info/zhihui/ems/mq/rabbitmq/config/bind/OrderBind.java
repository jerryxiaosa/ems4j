package info.zhihui.ems.mq.rabbitmq.config.bind;

import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static info.zhihui.ems.mq.rabbitmq.constant.QueueConstant.QUEUE_ORDER_TRY_TO_COMPLETE;

/**
 * @author jerryxiaosa
 */
@Configuration
public class OrderBind {
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(OrderConstant.ORDER_DELAY_DESTINATION, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue delayedQueue() {
        return new Queue(QueueConstant.QUEUE_ORDER_OVERDUE);
    }

    @Bean
    public Binding bindingOrderOverdue(@Qualifier("delayedQueue") Queue queue,@Qualifier("delayedExchange") CustomExchange customExchange) {
        return BindingBuilder.bind(queue).to(customExchange).with(OrderConstant.ROUTING_DELAYED_ORDER_CHECK).noargs();
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(OrderConstant.ORDER_DESTINATION);
    }

    @Bean
    public Queue orderTryToCompleteQueue() {
        return new Queue(QUEUE_ORDER_TRY_TO_COMPLETE);
    }

    /**
     * 尝试完成订单
     */
    @Bean
    public Binding orderTryToCompleteBinding(@Qualifier("orderTryToCompleteQueue") Queue queue, @Qualifier("orderExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(OrderConstant.ROUTING_KEY_ORDER_TRY_COMPLETE);
    }

    @Bean
    public Queue orderSuccessEnergyTopUpQueue() {
        return new Queue(QueueConstant.QUEUE_ORDER_SUCCESS_ENERGY_TOP_UP);
    }

    /**
     * 订单成功-电表充值成功
     */
    @Bean
    public Binding orderSuccessEnergyTopUpBinding(@Qualifier("orderSuccessEnergyTopUpQueue") Queue queue, @Qualifier("orderExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(OrderConstant.ROUTING_KEY_ORDER_STATUS_SUCCESS_ENERGY_TOP_UP);
    }

    @Bean
    public Queue orderSuccessTerminationQueue() {
        return new Queue(QueueConstant.QUEUE_ORDER_SUCCESS_TERMINATION);
    }

    /**
     * 订单成功-销户成功
     */
    @Bean
    public Binding orderSuccessTerminationBinding(@Qualifier("orderSuccessTerminationQueue") Queue queue, @Qualifier("orderExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(OrderConstant.ROUTING_KEY_ORDER_STATUS_SUCCESS_TERMINATION);
    }

    @Bean
    public Queue orderStatusChangeQueue() {
        return new Queue(QueueConstant.QUEUE_ORDER_STATUS_CHANGE);
    }

    /**
     * 订单状态变更
     */
    @Bean
    public Binding orderStatusChangeBinding(@Qualifier("orderExchange") TopicExchange topicExchange, Queue orderStatusChangeQueue) {
        return BindingBuilder.bind(orderStatusChangeQueue).to(topicExchange).with(OrderConstant.ROUTING_KEY_ORDER_STATUS);
    }
}