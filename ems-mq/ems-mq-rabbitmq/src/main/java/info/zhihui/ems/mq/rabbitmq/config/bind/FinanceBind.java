package info.zhihui.ems.mq.rabbitmq.config.bind;

import info.zhihui.ems.mq.api.constant.finance.FinanceMqConstant;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 财务相关 MQ 绑定配置
 *
 * @author jerryxiaosa
 */
@Configuration
public class FinanceBind {

    @Bean
    public TopicExchange financeExchange() {
        return new TopicExchange(FinanceMqConstant.FINANCE_DESTINATION);
    }

    @Bean
    public Queue balanceChangedQueue() {
        return new Queue(QueueConstant.QUEUE_FINANCE_BALANCE_CHANGED);
    }

    @Bean
    public Binding balanceChangedBinding(@Qualifier("financeExchange") TopicExchange financeExchange, Queue balanceChangedQueue) {
        return BindingBuilder.bind(balanceChangedQueue)
                .to(financeExchange)
                .with(FinanceMqConstant.ROUTING_KEY_BALANCE_CHANGED);
    }
}
