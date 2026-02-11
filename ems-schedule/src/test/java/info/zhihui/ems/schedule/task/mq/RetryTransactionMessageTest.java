package info.zhihui.ems.schedule.task.mq;

import info.zhihui.ems.mq.api.bo.TransactionMessageBo;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.service.MqService;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class RetryTransactionMessageTest {

    @Mock
    private TransactionMessageService transactionMessageService;

    @Mock
    private MqService mqService;

    private RetryTransactionMessage retryTransactionMessage;

    @BeforeEach
    void setUp() {
        retryTransactionMessage = new RetryTransactionMessage(transactionMessageService, mqService);
    }

    @Test
    @DisplayName("无失败消息时不应发送")
    void retryFailedTransactionMessages_EmptyMessages_ShouldSkipSend() {
        Mockito.when(transactionMessageService.findRecentFailureRecords()).thenReturn(List.of());

        retryTransactionMessage.retryFailedTransactionMessages();

        Mockito.verify(transactionMessageService, Mockito.never())
                .failure(Mockito.any(), Mockito.anyString());
        Mockito.verifyNoInteractions(mqService);
    }

    @Test
    @DisplayName("发送失败时应标记事务消息失败")
    void retryFailedTransactionMessages_SendFailed_ShouldMarkFailure() {
        TransactionMessageBo messageBo = new TransactionMessageBo()
                .setBusinessType(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT)
                .setSn("TEST-SN-001")
                .setMessage(new MqMessage()
                        .setMessageDestination("test-topic")
                        .setRoutingIdentifier("test-key")
                        .setPayload("test-payload"));
        Mockito.when(transactionMessageService.findRecentFailureRecords()).thenReturn(List.of(messageBo));
        Mockito.doThrow(new RuntimeException("mq send failed"))
                .when(mqService).sendMessage(Mockito.any(MqMessage.class));

        retryTransactionMessage.retryFailedTransactionMessages();

        Mockito.verify(transactionMessageService, Mockito.times(1))
                .failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-SN-001");
    }
}

