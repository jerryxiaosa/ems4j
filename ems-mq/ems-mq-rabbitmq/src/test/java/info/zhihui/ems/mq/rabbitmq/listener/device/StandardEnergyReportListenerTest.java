package info.zhihui.ems.mq.rabbitmq.listener.device;

import info.zhihui.ems.business.billing.constant.BillingConstant;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.message.device.StandardEnergyReportMessage;
import info.zhihui.ems.mq.rabbitmq.exception.NonRetryableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StandardEnergyReportListenerTest {

    @Mock
    private EnergyReportProcessor standardEnergyReportProcessor;

    @InjectMocks
    private StandardEnergyReportListener standardEnergyReportListener;

    @Test
    @DisplayName("接收到标准电量上报消息应原样委托给处理器")
    void testHandle_ShouldDelegateToProcessor() {
        StandardEnergyReportMessage message = buildMessage();

        standardEnergyReportListener.handle(message);

        ArgumentCaptor<StandardEnergyReportMessage> messageCaptor = ArgumentCaptor.forClass(StandardEnergyReportMessage.class);
        verify(standardEnergyReportProcessor).process(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isSameAs(message);
    }

    @Test
    @DisplayName("处理器抛出不可重试异常时监听器应记录后吞掉")
    void testHandle_WhenProcessorThrowsNonRetryableException_ShouldNotThrow() {
        StandardEnergyReportMessage message = buildMessage();
        doThrow(new NonRetryableException("invalid message"))
                .when(standardEnergyReportProcessor).process(message);

        assertThatCode(() -> standardEnergyReportListener.handle(message)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("处理器抛出业务运行时异常时监听器应继续抛出")
    void testHandle_WhenProcessorThrowsBusinessRuntimeException_ShouldRethrow() {
        StandardEnergyReportMessage message = buildMessage();
        doThrow(new BusinessRuntimeException("db timeout"))
                .when(standardEnergyReportProcessor).process(message);

        assertThatThrownBy(() -> standardEnergyReportListener.handle(message))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("db timeout");
    }

    @Test
    @DisplayName("处理器抛出重复上报异常时监听器应吞掉")
    void testHandle_WhenProcessorThrowsDuplicateBusinessRuntimeException_ShouldNotThrow() {
        StandardEnergyReportMessage message = buildMessage();
        doThrow(new BusinessRuntimeException(BillingConstant.DUPLICATE_POWER_RECORD_MESSAGE_PREFIX + "，originalReportId=abc"))
                .when(standardEnergyReportProcessor).process(message);

        assertThatCode(() -> standardEnergyReportListener.handle(message)).doesNotThrowAnyException();
    }

    private StandardEnergyReportMessage buildMessage() {
        return new StandardEnergyReportMessage()
                .setSource("IOT")
                .setSourceReportId("RPT-20260309-001")
                .setDeviceNo("DEV-001")
                .setRecordTime(LocalDateTime.of(2026, 3, 9, 15, 0, 0))
                .setTotalEnergy(new BigDecimal("123.45"))
                .setHigherEnergy(new BigDecimal("11.11"))
                .setHighEnergy(new BigDecimal("22.22"))
                .setLowEnergy(new BigDecimal("33.33"))
                .setLowerEnergy(new BigDecimal("44.44"))
                .setDeepLowEnergy(new BigDecimal("12.35"));
    }
}
