package info.zhihui.ems.mq.rabbitmq.config;

import info.zhihui.ems.mq.api.service.MqService;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import info.zhihui.ems.mq.rabbitmq.config.properties.TransactionMessageRetryProperties;
import info.zhihui.ems.mq.rabbitmq.repository.TransactionMessageRepository;
import info.zhihui.ems.mq.rabbitmq.service.impl.RabbitMqServiceImpl;
import info.zhihui.ems.mq.rabbitmq.service.impl.TransactionMessageServiceImpl;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author jerryxiaosa
 */
@Configuration
@EnableConfigurationProperties(TransactionMessageRetryProperties.class)
public class RabbitMqConfig {
    @Value("${spring.rabbitmq.listener.simple.concurrency}")
    private Integer concurrency;

    @Value("${spring.rabbitmq.listener.simple.max-concurrency}")
    private Integer maxConcurrency;

    @Value("${spring.rabbitmq.listener.simple.prefetch}")
    private Integer prefetch;

    @Value("${spring.rabbitmq.listener.simple.retry.max-attempts}")
    private Integer maxAttempts;

    @Value("${spring.rabbitmq.listener.simple.retry.initial-interval}")
    private Integer initialInterval;

    @Value("${spring.rabbitmq.listener.simple.retry.max-interval}")
    private Integer maxInterval;

    @Value("${spring.rabbitmq.listener.simple.retry.multiplier}")
    private Integer multiplier;

    @Bean
    @ConditionalOnProperty(name = "mq.type", havingValue = "rabbitmq", matchIfMissing = true)
    public TransactionMessageService getTransactionMessageService(TransactionMessageRepository repository,
                                                                  TransactionMessageRetryProperties retryProperties) {
        return new TransactionMessageServiceImpl(repository,
                retryProperties.getMaxRetryTimes(),
                retryProperties.getFetchSize());
    }

    @Bean
    @ConditionalOnProperty(name = "mq.type", havingValue = "rabbitmq", matchIfMissing = true)
    public MqService mqService(AmqpTemplate rabbitTemplate, TransactionMessageService transactionMessageService) {
        return new RabbitMqServiceImpl(rabbitTemplate, transactionMessageService);
    }

    @Bean
    @ConditionalOnClass(RabbitMqServiceImpl.class)
    @ConditionalOnProperty(name = "mq.type", havingValue = "rabbitmq", matchIfMissing = true)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    @Bean
    @ConditionalOnProperty(name = "mq.type", havingValue = "rabbitmq", matchIfMissing = true)
    public TaskScheduler rabbitMqTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("rabbitMq-retry-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    @ConditionalOnProperty(name = "mq.type", havingValue = "rabbitmq", matchIfMissing = true)
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(concurrency);
        factory.setMaxConcurrentConsumers(maxConcurrency);
        factory.setPrefetchCount(prefetch);

        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(maxAttempts)
                .backOffOptions(initialInterval, multiplier, maxInterval)
                .build());

        return factory;
    }
}
