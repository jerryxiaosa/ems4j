package info.zhihui.ems.mq.rabbitmq.listener.order.success;

import info.zhihui.ems.business.finance.dto.BalanceDeleteDto;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.message.order.status.TerminationSuccessMessage;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * TerminationListener 单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单终止监听器测试")
class TerminationListenerTest {

    @Mock
    private BalanceService balanceService;

    @Mock
    private TransactionMessageService transactionMessageService;

    @InjectMocks
    private TerminationListener terminationListener;

    private TerminationSuccessMessage terminationSuccessMessage;

    @BeforeEach
    void setUp() {
        // Given: 准备基础测试数据
        terminationSuccessMessage = new TerminationSuccessMessage();
        terminationSuccessMessage.setAccountId(12345)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricMeterAmount(2)
                .setFullCancel(true)
                .setMeterIdList(List.of(1001, 1002))
                .setOrderSn("TEST-ORDER-001")
                .setOrderStatus("SUCCESS");
    }

    @Test
    @DisplayName("按需计费账户销户成功测试 - 每个电表单独销户")
    void handleTerminationSuccess_QuantityAccount_Success() {
        // Given: 按需计费账户消息
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        // When: 调用处理方法
        assertDoesNotThrow(() -> {
            terminationListener.handleTerminationSuccess(message);
        });

        // Then: 验证每个电表都被单独销户
        verify(balanceService, times(2)).deleteBalance(any(BalanceDeleteDto.class));
        verify(balanceService).deleteBalance(argThat(dto ->
                dto.getBalanceRelationId().equals(1001) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER));
        verify(balanceService).deleteBalance(argThat(dto ->
                dto.getBalanceRelationId().equals(1002) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER));

        // 验证事务消息标记成功
        verify(transactionMessageService, times(1))
                .success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("包月计费账户fullCancel=true时销户成功测试")
    void handleTerminationSuccess_MonthlyAccount_FullCancel_Success() {
        // Given: 包月计费账户，fullCancel=true
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setFullCancel(true);

        // When: 调用处理方法
        assertDoesNotThrow(() -> {
            terminationListener.handleTerminationSuccess(message);
        });

        // Then: 验证整个账户被销户
        verify(balanceService, times(1)).deleteBalance(argThat(dto ->
                dto.getBalanceRelationId().equals(12345) && dto.getBalanceType() == BalanceTypeEnum.ACCOUNT));

        // 验证事务消息标记成功
        verify(transactionMessageService, times(1))
                .success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("合并计费账户fullCancel=true时销户成功测试")
    void handleTerminationSuccess_MergedAccount_FullCancel_Success() {
        // Given: 合并计费账户，fullCancel=true
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setFullCancel(true);

        // When: 调用处理方法
        assertDoesNotThrow(() -> {
            terminationListener.handleTerminationSuccess(message);
        });

        // Then: 验证调用账户级别的余额删除
        verify(balanceService, times(1)).deleteBalance(argThat(dto ->
                dto.getBalanceRelationId().equals(12345) && dto.getBalanceType() == BalanceTypeEnum.ACCOUNT));

        // 验证事务消息标记成功
        verify(transactionMessageService, times(1))
                .success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("包月计费账户fullCancel=false时跳过销户测试")
    void handleTerminationSuccess_MonthlyAccount_NotFullCancel_Skip() {
        // Given: 包月计费账户，fullCancel=false
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setFullCancel(false);

        // When: 调用处理方法
        assertDoesNotThrow(() -> {
            terminationListener.handleTerminationSuccess(message);
        });

        // Then: 验证没有调用删除余额方法
        verify(balanceService, never()).deleteBalance(any(BalanceDeleteDto.class));

        // 验证事务消息标记成功
        verify(transactionMessageService, times(1))
                .success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("合并计费账户fullCancel=false时跳过销户测试")
    void handleTerminationSuccess_MergedAccount_NotFullCancel_Skip() {
        // Given: 合并计费账户，fullCancel=false
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setFullCancel(false);

        // When: 调用处理方法
        assertDoesNotThrow(() -> {
            terminationListener.handleTerminationSuccess(message);
        });

        // Then: 验证没有调用删除余额方法
        verify(balanceService, never()).deleteBalance(any(BalanceDeleteDto.class));

        // 验证事务消息标记成功
        verify(transactionMessageService, times(1))
                .success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("参数校验失败测试 - 电表数量不一致")
    void handleTerminationSuccess_ValidationFailed_MeterCountMismatch() {
        // Given: 电表数量不一致的消息
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricMeterAmount(3) // 实际电表列表只有2个
                .setMeterIdList(List.of(1001, 1002));

        terminationListener.handleTerminationSuccess(message);

        // 验证没有调用删除余额方法
        verify(balanceService, never()).deleteBalance(any(BalanceDeleteDto.class));

        // 验证事务消息标记失败
        verify(transactionMessageService, times(1))
                .failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("参数校验失败测试 - 电表ID列表为空")
    void handleTerminationSuccess_ValidationFailed_EmptyMeterList() {
        // Given: 电表ID列表为空的消息
        TerminationSuccessMessage message = terminationSuccessMessage
                .setMeterIdList(Collections.emptyList())
                .setElectricMeterAmount(1);

        terminationListener.handleTerminationSuccess(message);

        // 验证没有调用删除余额方法
        verify(balanceService, never()).deleteBalance(any(BalanceDeleteDto.class));

        // 验证事务消息标记失败
        verify(transactionMessageService, times(1))
                .failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("按需计费部分电表删除失败测试")
    void handleTerminationSuccess_QuantityAccount_PartialFailure() {
        // Given: 按需计费账户，第二个电表删除失败
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        doNothing().when(balanceService).deleteBalance(argThat(dto ->
                dto.getBalanceRelationId().equals(1001) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER));
        doThrow(new RuntimeException("删除电表余额失败"))
                .when(balanceService).deleteBalance(argThat(dto ->
                        dto.getBalanceRelationId().equals(1002) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER));

        terminationListener.handleTerminationSuccess(message);

        // 验证第一个电表删除成功，第二个电表删除失败
        verify(balanceService, times(1)).deleteBalance(argThat(dto ->
                dto.getBalanceRelationId().equals(1001) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER));
        verify(balanceService, times(1)).deleteBalance(argThat(dto ->
                dto.getBalanceRelationId().equals(1002) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER));

        // 验证事务消息标记失败
        verify(transactionMessageService, times(1))
                .failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("账户余额删除失败测试")
    void handleTerminationSuccess_AccountBalanceDeleteFailure() {
        // Given: 包月计费账户，账户余额删除失败
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setFullCancel(true);

        doThrow(new RuntimeException("删除账户余额失败"))
                .when(balanceService).deleteBalance(argThat(dto ->
                        dto.getBalanceRelationId().equals(12345) && dto.getBalanceType() == BalanceTypeEnum.ACCOUNT));

        terminationListener.handleTerminationSuccess(message);

        // 验证调用了删除账户余额方法
        verify(balanceService, times(1)).deleteBalance(argThat(dto ->
                dto.getBalanceRelationId().equals(12345) && dto.getBalanceType() == BalanceTypeEnum.ACCOUNT));

        // 验证事务消息标记失败
        verify(transactionMessageService, times(1))
                .failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("事务消息成功标记失败测试")
    void handleTerminationSuccess_TransactionMessageSuccessFailure() {
        // Given: 按需计费账户，事务消息标记成功失败
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        doThrow(new RuntimeException("标记事务消息成功失败"))
                .when(transactionMessageService).success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");

        terminationListener.handleTerminationSuccess(message);

        // 验证删除余额方法被调用
        verify(balanceService, times(2)).deleteBalance(any(BalanceDeleteDto.class));

        // 验证事务消息标记成功被调用
        verify(transactionMessageService, times(1))
                .success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");

        // 验证事务消息标记失败被调用
        verify(transactionMessageService, times(1))
                .failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }

    @Test
    @DisplayName("事务消息失败标记失败测试")
    void handleTerminationSuccess_TransactionMessageFailureFailure() {
        // Given: 参数校验失败，且事务消息标记失败也失败
        TerminationSuccessMessage message = terminationSuccessMessage
                .setElectricMeterAmount(3) // 电表数量不一致
                .setMeterIdList(List.of(1001, 1002));

        doThrow(new BusinessRuntimeException("标记事务消息失败失败"))
                .when(transactionMessageService).failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");

        // When & Then: 验证抛出业务异常（原始异常）
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            terminationListener.handleTerminationSuccess(message);
        });

        assertEquals("标记事务消息失败失败", exception.getMessage());

        // 验证没有调用删除余额方法
        verify(balanceService, never()).deleteBalance(any(BalanceDeleteDto.class));

        // 验证事务消息标记失败被调用
        verify(transactionMessageService, times(1))
                .failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-ORDER-001");
    }
}