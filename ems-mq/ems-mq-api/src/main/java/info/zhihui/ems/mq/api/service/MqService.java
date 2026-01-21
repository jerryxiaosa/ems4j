package info.zhihui.ems.mq.api.service;

import info.zhihui.ems.common.model.MqMessage;
import info.zhihui.ems.mq.api.dto.TransactionMessageDto;

/**
 * @author jerryxiaosa
 */
public interface MqService {
    /**
     * 发送消息
     *
     * @param mqMessage 待发送的消息
     */
    void sendMessage(MqMessage mqMessage);

    /**
     * 事务提交后发送消息
     * 不影响原先事务，不会报错
     *
     * @param mqMessage 待发送的消息
     */
    void sendMessageAfterCommit(MqMessage mqMessage);

    /**
     * 发送事务消息，有重试机制
     *
     * @param transactionMessageDto 事务消息
     */
    void sendTransactionMessage(TransactionMessageDto transactionMessageDto);
}
