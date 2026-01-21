package info.zhihui.ems.mq.rabbitmq.listener.order.success;

import info.zhihui.ems.business.device.dto.ElectricMeterSwitchStatusDto;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    private ElectricMeterManagerService electricMeterManagerService;

    @Mock
    private TransactionMessageService transactionMessageService;

    @InjectMocks
    private EnergyTopUpListener energyTopUpListener;

    private EnergyTopUpSuccessMessage accountTopUpMessage;
    private EnergyTopUpSuccessMessage electricMeterTopUpMessage;

    @BeforeEach
    void setUp() {
        // 准备账户充值测试数据
        accountTopUpMessage = new EnergyTopUpSuccessMessage();
        accountTopUpMessage.setAccountId(100)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderSn("ORDER-ACCOUNT-001");

        // 准备电表充值测试数据
        electricMeterTopUpMessage = new EnergyTopUpSuccessMessage();
        electricMeterTopUpMessage.setAccountId(1002)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setMeterId(2001)
                .setMeterType(MeterTypeEnum.ELECTRIC)
                .setOrderAmount(new BigDecimal("200.00"))
                .setOrderSn("ORDER-METER-001");
    }

    @Test
    @DisplayName("处理账户充值成功消息 - 成功场景")
    void handle_AccountTopUp_Success() {
        // Given: 模拟服务调用成功
        doNothing().when(balanceService).topUp(any(BalanceDto.class));
        doNothing().when(transactionMessageService).success(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-ACCOUNT-001"));

        // When: 调用处理方法
        Assertions.assertDoesNotThrow(() -> {
            energyTopUpListener.handle(accountTopUpMessage);
        });

        // Then: 验证服务调用
        ArgumentCaptor<BalanceDto> topUpCaptor = ArgumentCaptor.forClass(BalanceDto.class);
        verify(balanceService, times(1)).topUp(topUpCaptor.capture());

        BalanceDto capturedDto = topUpCaptor.getValue();
        Assertions.assertEquals(100, capturedDto.getBalanceRelationId());
        Assertions.assertEquals(BalanceTypeEnum.ACCOUNT, capturedDto.getBalanceType());
        Assertions.assertEquals(100, capturedDto.getAccountId());
        Assertions.assertEquals("ORDER-ACCOUNT-001", capturedDto.getOrderNo());
        Assertions.assertEquals(new BigDecimal("100.00"), capturedDto.getAmount());

        verify(transactionMessageService, times(1)).success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
        verify(electricMeterManagerService, never()).setSwitchStatus(any());
    }

    @Test
    @DisplayName("处理电表充值成功消息 - 成功场景")
    void handle_ElectricMeterTopUp_Success() {
        // Given: 模拟服务调用成功
        doNothing().when(balanceService).topUp(any(BalanceDto.class));
        doNothing().when(electricMeterManagerService).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
        doNothing().when(transactionMessageService).success(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-METER-001"));

        // When: 调用处理方法
        Assertions.assertDoesNotThrow(() -> {
            energyTopUpListener.handle(electricMeterTopUpMessage);
        });

        // Then: 验证服务调用
        ArgumentCaptor<BalanceDto> topUpCaptor = ArgumentCaptor.forClass(BalanceDto.class);
        verify(balanceService, times(1)).topUp(topUpCaptor.capture());

        BalanceDto capturedDto = topUpCaptor.getValue();
        Assertions.assertEquals(2001, capturedDto.getBalanceRelationId());
        Assertions.assertEquals(BalanceTypeEnum.ELECTRIC_METER, capturedDto.getBalanceType());
        Assertions.assertEquals(1002, capturedDto.getAccountId());
        Assertions.assertEquals("ORDER-METER-001", capturedDto.getOrderNo());
        Assertions.assertEquals(new BigDecimal("200.00"), capturedDto.getAmount());

        ArgumentCaptor<ElectricMeterSwitchStatusDto> switchCaptor = ArgumentCaptor.forClass(ElectricMeterSwitchStatusDto.class);
        verify(electricMeterManagerService, times(1)).setSwitchStatus(switchCaptor.capture());

        ElectricMeterSwitchStatusDto capturedSwitchDto = switchCaptor.getValue();
        Assertions.assertEquals(2001, capturedSwitchDto.getId());
        Assertions.assertEquals(ElectricSwitchStatusEnum.ON, capturedSwitchDto.getSwitchStatus());

        verify(transactionMessageService, times(1)).success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-METER-001");
    }

    @Test
    @DisplayName("处理不支持的充值类型 - 异常场景")
    void handle_UnsupportedBalanceType_Exception() {
        // Given: 准备不支持的充值类型消息
        EnergyTopUpSuccessMessage unsupportedMessage = new EnergyTopUpSuccessMessage();
        unsupportedMessage.setOrderAmount(new BigDecimal("50.00"))
                .setBalanceType(null) // 不支持的类型
                .setAccountId(1003)
                .setOrderSn("ORDER-UNSUPPORTED-001");

        doNothing().when(transactionMessageService).failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-UNSUPPORTED-001"));

        // When: 调用处理方法
        Assertions.assertDoesNotThrow(() -> {
            energyTopUpListener.handle(unsupportedMessage);
        });

        // Then: 验证异常处理
        verify(balanceService, never()).topUp(any());
        verify(electricMeterManagerService, never()).setSwitchStatus(any());
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-UNSUPPORTED-001");
    }

    @Test
    @DisplayName("账户充值过程中发生异常 - 异常场景")
    void handle_AccountTopUp_BalanceServiceException() {
        // Given: 模拟充值服务抛出异常
        doThrow(new BusinessRuntimeException("余额不足")).when(balanceService).topUp(any(BalanceDto.class));
        doNothing().when(transactionMessageService).failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-ACCOUNT-001"));

        // When: 调用处理方法
        Assertions.assertDoesNotThrow(() -> {
            energyTopUpListener.handle(accountTopUpMessage);
        });

        // Then: 验证异常处理
        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
    }

    @Test
    @DisplayName("电表充值过程中发生异常 - 异常场景")
    void handle_ElectricMeterTopUp_BalanceServiceException() {
        // Given: 模拟充值服务抛出异常
        doThrow(new BusinessRuntimeException("电表不存在")).when(balanceService).topUp(any(BalanceDto.class));
        doNothing().when(transactionMessageService).failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-METER-001"));

        // When: 调用处理方法
        Assertions.assertDoesNotThrow(() -> {
            energyTopUpListener.handle(electricMeterTopUpMessage);
        });

        // Then: 验证异常处理
        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(electricMeterManagerService, never()).setSwitchStatus(any());
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-METER-001");
    }

    @Test
    @DisplayName("电表开关操作异常 - 异常场景")
    void handle_ElectricMeterTopUp_SwitchOperationException() {
        // Given: 模拟充值成功但开关操作失败
        doNothing().when(balanceService).topUp(any(BalanceDto.class));
        doThrow(new BusinessRuntimeException("开关操作失败")).when(electricMeterManagerService).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
        doNothing().when(transactionMessageService).failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-METER-001"));

        // When: 调用处理方法
        Assertions.assertDoesNotThrow(() -> {
            energyTopUpListener.handle(electricMeterTopUpMessage);
        });

        // Then: 验证异常处理
        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(electricMeterManagerService, times(1)).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-METER-001");
    }

    @Test
    @DisplayName("事务消息标记成功失败 - 异常场景")
    void handle_TransactionMessageSuccessException() {
        // Given: 模拟充值成功但事务消息标记失败
        doNothing().when(balanceService).topUp(any(BalanceDto.class));
        doThrow(new BusinessRuntimeException("事务消息更新失败")).when(transactionMessageService).success(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-ACCOUNT-001"));

        // When: 调用处理方法
        Assertions.assertDoesNotThrow(() -> {
            energyTopUpListener.handle(accountTopUpMessage);
        });

        // Then: 验证异常处理
        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(transactionMessageService, times(1)).success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
    }

    @Test
    @DisplayName("事务消息标记失败也失败 - 双重异常场景")
    void handle_TransactionMessageDoubleException() {
        // Given: 模拟充值失败，且事务消息标记失败也失败
        doThrow(new BusinessRuntimeException("充值失败")).when(balanceService).topUp(any(BalanceDto.class));
        doThrow(new BusinessRuntimeException("事务消息标记失败也失败")).when(transactionMessageService).failure(eq(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT), eq("ORDER-ACCOUNT-001"));

        // When: 调用处理方法
        Assertions.assertDoesNotThrow(() -> {
            energyTopUpListener.handle(accountTopUpMessage);
        });

        // Then: 验证异常处理
        verify(balanceService, times(1)).topUp(any(BalanceDto.class));
        verify(transactionMessageService, never()).success(any(), any());
        verify(transactionMessageService, times(1)).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "ORDER-ACCOUNT-001");
    }

}