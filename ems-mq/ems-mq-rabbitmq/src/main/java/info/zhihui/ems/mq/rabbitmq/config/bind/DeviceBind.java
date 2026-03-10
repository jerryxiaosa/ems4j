package info.zhihui.ems.mq.rabbitmq.config.bind;

import info.zhihui.ems.mq.api.constant.device.DeviceMqConstant;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 设备相关 MQ 绑定配置
 *
 * @author jerryxiaosa
 */
@Configuration
public class DeviceBind {

    @Bean
    public TopicExchange deviceExchange() {
        return new TopicExchange(DeviceMqConstant.DEVICE_DESTINATION);
    }

    @Bean
    public Queue standardEnergyReportQueue() {
        return new Queue(QueueConstant.QUEUE_DEVICE_STANDARD_ENERGY_REPORT);
    }

    @Bean
    public Binding standardEnergyReportBinding(@Qualifier("deviceExchange") TopicExchange deviceExchange,
                                               @Qualifier("standardEnergyReportQueue") Queue standardEnergyReportQueue) {
        return BindingBuilder.bind(standardEnergyReportQueue)
                .to(deviceExchange)
                .with(DeviceMqConstant.ROUTING_KEY_STANDARD_ENERGY_REPORT);
    }
}
