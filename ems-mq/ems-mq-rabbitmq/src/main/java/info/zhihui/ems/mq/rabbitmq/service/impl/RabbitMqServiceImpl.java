package info.zhihui.ems.mq.rabbitmq.service.impl;

import info.zhihui.ems.common.model.MqMessage;
import info.zhihui.ems.common.utils.TransactionUtil;
import info.zhihui.ems.mq.api.dto.TransactionMessageDto;
import info.zhihui.ems.mq.api.message.order.delay.BaseDelayMessage;
import info.zhihui.ems.mq.api.service.MqService;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;

/**
 * @author jerryxiaosa
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMqServiceImpl implements MqService {

    private final AmqpTemplate rabbitTemplate;
    private final TransactionMessageService transactionMessageService;

    @Override
    public void sendMessage(MqMessage mqMessage) {
        log.debug("发送消息，消息目的地: {}, 路由标识: {}, 消息内容: {}",
                mqMessage.getMessageDestination(),
                mqMessage.getRoutingIdentifier(),
                mqMessage.getPayload());

        // 延时消息
        if (mqMessage.getPayload() instanceof BaseDelayMessage delayMessage) {
            rabbitTemplate.convertAndSend(mqMessage.getMessageDestination(), mqMessage.getRoutingIdentifier(), mqMessage.getPayload(),
                    a -> {
                        a.getMessageProperties().setDelayLong(delayMessage.getDelaySeconds() * 1000);
                        return a;
                    });
        } else {
            rabbitTemplate.convertAndSend(mqMessage.getMessageDestination(), mqMessage.getRoutingIdentifier(), mqMessage.getPayload());
        }

    }

    @Override
    public void sendMessageAfterCommit(MqMessage mqMessage) {
        TransactionUtil.afterCommitSyncExecute(() -> {
            try {
                sendMessage(mqMessage);
            } catch (Exception e) {
                log.error("发送消息失败: {}", mqMessage, e);
            }
        });
    }

    @Override
    public void sendTransactionMessage(TransactionMessageDto transactionMessageDto) {
        transactionMessageService.add(transactionMessageDto);

        sendMessageAfterCommit(transactionMessageDto.getMessage());
    }

}
