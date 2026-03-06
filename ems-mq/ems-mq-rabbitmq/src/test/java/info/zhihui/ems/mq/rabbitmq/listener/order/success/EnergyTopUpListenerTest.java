package info.zhihui.ems.mq.rabbitmq.listener.order.success;

import info.zhihui.ems.business.finance.dto.BalanceDto;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.message.order.status.EnergyTopUpSuccessMessage;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * EnergyTopUpListener 单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("能源充值成功消息监听器测试")
class EnergyTopUpListenerTest {

    @Mock
    private BalanceService balanceService;

    @Mock
    private TransactionMessageService transactionMessageService;

    @InjectMocks
    private EnergyTopUpListener energyTopUpListener;

    private EnergyTopUpSuccessMessage accountTopUpMessage;
    private EnergyTopUpSuccessMessage electricMeterTopUpMessage;

    @BeforeEach
    void setUp() {
        accountTopUpMessage = new EnergyTopUpSuccessMessage();
        accountTopUpMessage.setAccountId(100)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderSn("ORDER-ACCOUNT-001");

        electricMeterTopUpMessage = new EnergyTopUpSuccessMessage();
        electricMeterTopUpMessage.setAccountId(1002)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setMeterId(2001)
                .setMeterType(MeterTypeEnum.ELECTRIC)
                .setOrderAmount(new BigDecimal("200.00"))
                .setOrderSn("ORDER-METER-001");
    }

    @Test
    void testHandle_WhenAccountTopUpSuccess_ShouldTopUpAndMarkSuccess() {
        doNothing().when(balanceService).topUp(any(BalanceDto.class));
        doNothing().when(transactionMessageService).success(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-ACCOUNT-001"));

        Assertions.assertDoesNotThrow(() -> energyTopUpListener.handle(accountTopUpMessage));

        ArgumentCaptor<BalanceDto> topUpCaptor = ArgumentCaptor.forClass(BalanceDto.class);
        verify(balanceService, times(1)).topUp(topUpCaptor.capture());

        BalanceDto capturedDto = topUpCaptor.getValue();
        Assertions.assertEquals(100, capturedDto.getBalanceRelationId());
        Assertions.assertEquals(BalanceTypeEnum.ACCOUNT, capturedDto.getBalanceType());
        Assertions.assertEquals(100, capturedDto.getAccountId());
        Assertions.assertEquals("ORDER-ACCOUNT-001", capturedDto.getOrderNo());
        Assertions.assertEquals(new BigDecimal("100.00"), capturedDto.getAmount());

        verify(transactionMessageService, times(1)).success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
    }

    @Test
    void testHandle_WhenElectricMeterTopUpSuccess_ShouldTopUpAndMarkSuccess() {
        doNothing().when(balanceService).topUp(any(BalanceDto.class));
        doNothing().when(transactionMessageService).success(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-METER-001"));

        Assertions.assertDoesNotThrow(() -> energyTopUpListener.handle(electricMeterTopUpMessage));

        ArgumentCaptor<BalanceDto> topUpCaptor = ArgumentCaptor.forClass(BalanceDto.class);
        verify(balanceService, times(1)).topUp(topUpCaptor.capture());

        BalanceDto capturedDto = topUpCaptor.getValue();
        Assertions.assertEquals(2001, capturedDto.getBalanceRelationId());
        Assertions.assertEquals(BalanceTypeEnum.ELECTRIC_METER, capturedDto.getBalanceType());
        Assertions.assertEquals(1002, capturedDto.getAccountId());
        Assertions.assertEquals("ORDER-METER-001", capturedDto.getOrderNo());
        Assertions.assertEquals(new BigDecimal("200.00"), capturedDto.getAmount());

        verify(transactionMessageService, times(1)).success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-METER-001");
    }

    @Test
    void testHandle_WhenBalanceTypeUnsupported_ShouldMarkFailure() {
        EnergyTopUpSuccessMessage unsupportedMessage = new EnergyTopUpSuccessMessage();
        unsupportedMessage.setOrderAmount(new BigDecimal("50.00"))
                .setBalanceType(null)
                .setAccountId(1003)
                .setOrderSn("ORDER-UNSUPPORTED-001");

        doNothing().when(transactionMessageService).failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-UNSUPPORTED-001"));

        Assertions.assertDoesNotThrow(() -> energyTopUpListener.handle(unsupportedMessage));

        verify(balanceService, never()).topUp(any());
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-UNSUPPORTED-001");
    }

    @Test
    void testHandle_WhenAccountTopUpFailed_ShouldMarkFailure() {
        doThrow(new BusinessRuntimeException("余额不足")).when(balanceService).topUp(any(BalanceDto.class));
        doNothing().when(transactionMessageService).failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-ACCOUNT-001"));

        Assertions.assertDoesNotThrow(() -> energyTopUpListener.handle(accountTopUpMessage));

        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
    }

    @Test
    void testHandle_WhenElectricMeterTopUpFailed_ShouldMarkFailure() {
        doThrow(new BusinessRuntimeException("电表不存在")).when(balanceService).topUp(any(BalanceDto.class));
        doNothing().when(transactionMessageService).failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-METER-001"));

        Assertions.assertDoesNotThrow(() -> energyTopUpListener.handle(electricMeterTopUpMessage));

        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-METER-001");
    }

    @Test
    void testHandle_WhenMarkSuccessFailed_ShouldMarkFailure() {
        doNothing().when(balanceService).topUp(any(BalanceDto.class));
        doThrow(new BusinessRuntimeException("事务消息更新失败")).when(transactionMessageService)
                .success(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-ACCOUNT-001"));

        Assertions.assertDoesNotThrow(() -> energyTopUpListener.handle(accountTopUpMessage));

        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(transactionMessageService, times(1)).success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
    }

    @Test
    void testHandle_WhenTopUpAndMarkFailureFailed_ShouldSwallowException() {
        doThrow(new BusinessRuntimeException("充值失败")).when(balanceService).topUp(any(BalanceDto.class));
        doThrow(new BusinessRuntimeException("事务消息标记失败也失败")).when(transactionMessageService)
                .failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-ACCOUNT-001"));

        Assertions.assertDoesNotThrow(() -> energyTopUpListener.handle(accountTopUpMessage));

        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
    }
}
