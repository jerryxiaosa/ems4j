package info.zhihui.ems.test.config;

import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.dto.TransactionMessageDto;
import info.zhihui.ems.mq.api.service.MqService;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import info.zhihui.ems.mq.rabbitmq.repository.TransactionMessageRepository;
import info.zhihui.ems.mq.rabbitmq.service.impl.TransactionMessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 集成测试环境下的 MQ 模拟实现，避免依赖真实 RabbitMQ。
 */
@TestConfiguration
@Profile("integrationtest")
public class MockMqConfig {
    private static final int DEFAULT_MAX_RETRY_TIMES = 10;
    private static final int DEFAULT_FETCH_SIZE = 100;

    @Bean
    @Primary
    public TransactionMessageService mockTransactionMessageService(TransactionMessageRepository transactionMessageRepository) {
        return new TransactionMessageServiceImpl(transactionMessageRepository, DEFAULT_MAX_RETRY_TIMES, DEFAULT_FETCH_SIZE);
    }

    @Bean
    @Primary
    public MqService mockMqService(TransactionMessageService transactionMessageService) {
        return new NoOpMqService(transactionMessageService);
    }


    /**
     * MQ 服务空实现，打印日志以便调试。
     */
    @Slf4j
    static class NoOpMqService implements MqService {

        private final TransactionMessageService transactionMessageService;

        NoOpMqService(TransactionMessageService transactionMessageService) {
            this.transactionMessageService = transactionMessageService;
        }

        @Override
        public void sendMessage(MqMessage mqMessage) {
            log.debug("[mock-mq] sendMessage: {}", mqMessage);
        }

        @Override
        public void sendMessageAfterCommit(MqMessage mqMessage) {
            log.debug("[mock-mq] sendMessageAfterCommit: {}", mqMessage);
        }

        @Override
        public void sendTransactionMessage(TransactionMessageDto transactionMessageDto) {
            log.debug("[mock-mq] sendTransactionMessage: {}", transactionMessageDto);
            transactionMessageService.add(transactionMessageDto);
        }
    }
}
