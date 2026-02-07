package info.zhihui.ems.business.finance.service.balance;

import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceDeleteDto;
import info.zhihui.ems.business.finance.dto.BalanceDto;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.entity.BalanceEntity;
import info.zhihui.ems.business.finance.entity.OrderFlowEntity;
import info.zhihui.ems.business.finance.qo.BalanceQo;
import info.zhihui.ems.business.finance.repository.BalanceRepository;
import info.zhihui.ems.business.finance.repository.OrderFlowRepository;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.service.MqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BalanceServiceImpl 单元测试类
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private OrderFlowRepository orderFlowRepository;

    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    @Mock
    private MqService mqService;

    private BalanceDto topUpDto;
    private BalanceDto deductDto;
    private BalanceQueryDto queryDto;
    private BalanceDeleteDto deleteDto;
    private BalanceEntity balanceEntity;

    @BeforeEach
    void setUp() {
        // 初始化充值DTO
        topUpDto = new BalanceDto()
                .setOrderNo("ORDER123456")
                .setAmount(BigDecimal.valueOf(100.00))
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(1);

        // 初始化扣费DTO
        deductDto = new BalanceDto()
                .setOrderNo("DEDUCT123456")
                .setAmount(BigDecimal.valueOf(50.00))
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(1);

        // 初始化查询DTO
        queryDto = new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(1);

        // 初始化删除DTO
        deleteDto = new BalanceDeleteDto()
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT);

        // 初始化余额实体
        balanceEntity = new BalanceEntity()
                .setId(1)
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode())
                .setAccountId(1)
                .setBalance(BigDecimal.valueOf(500.00));
    }

    /**
     * 测试正常充值场景
     */
    @Test
    @DisplayName("测试正常充值场景")
    void testTopUp_Normal() {
        // Given
        when(orderFlowRepository.insert(any(OrderFlowEntity.class))).thenReturn(1);
        when(balanceRepository.balanceTopUp(any(BalanceQo.class))).thenReturn(1);
        when(balanceRepository.balanceQuery(any(BalanceQo.class))).thenReturn(balanceEntity);

        // When
        assertDoesNotThrow(() -> balanceService.topUp(topUpDto));

        // Then
        verify(orderFlowRepository).insert(any(OrderFlowEntity.class));
        verify(balanceRepository).balanceTopUp(any(BalanceQo.class));
        verify(mqService).sendMessageAfterCommit(any(MqMessage.class));
    }

    /**
     * 测试重复订单充值异常
     */
    @Test
    @DisplayName("测试重复订单充值异常")
    void testTopUp_DuplicateOrder() {
        // Given
        when(orderFlowRepository.insert(any(OrderFlowEntity.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.topUp(topUpDto));
        assertEquals("订单不能重复操作，订单号：ORDER123456", exception.getMessage());

        verify(orderFlowRepository).insert(any(OrderFlowEntity.class));
        verify(balanceRepository, never()).balanceTopUp(any(BalanceQo.class));
    }

    /**
     * 测试余额更新失败异常
     */
    @Test
    @DisplayName("测试余额更新失败异常")
    void testTopUp_BalanceUpdateFailed() {
        // Given
        when(orderFlowRepository.insert(any(OrderFlowEntity.class))).thenReturn(1);
        when(balanceRepository.balanceTopUp(any(BalanceQo.class))).thenReturn(0);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.topUp(topUpDto));
        assertEquals("账户余额结算异常，请重试", exception.getMessage());

        verify(orderFlowRepository).insert(any(OrderFlowEntity.class));
        verify(balanceRepository).balanceTopUp(any(BalanceQo.class));
        verify(mqService, never()).sendMessageAfterCommit(any());
    }

    /**
     * 测试余额更新返回null异常
     */
    @Test
    @DisplayName("测试余额更新返回null异常")
    void testTopUp_BalanceUpdateReturnNull() {
        // Given
        when(orderFlowRepository.insert(any(OrderFlowEntity.class))).thenReturn(1);
        when(balanceRepository.balanceTopUp(any(BalanceQo.class))).thenReturn(null);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.topUp(topUpDto));
        assertEquals("账户余额结算异常，请重试", exception.getMessage());

        verify(orderFlowRepository).insert(any(OrderFlowEntity.class));
        verify(balanceRepository).balanceTopUp(any(BalanceQo.class));
        verify(mqService, never()).sendMessageAfterCommit(any());
    }

    /**
     * 测试正常查询余额场景
     */
    @Test
    @DisplayName("测试正常查询余额场景")
    void testQuery_Normal() {
        // Given
        when(balanceRepository.balanceQuery(any(BalanceQo.class))).thenReturn(balanceEntity);

        // When
        BalanceBo result = balanceService.query(queryDto);

        // Then
        assertNotNull(result);
        assertEquals(balanceEntity.getId(), result.getId());
        assertEquals(balanceEntity.getBalanceRelationId(), result.getBalanceRelationId());
        assertEquals(BalanceTypeEnum.ACCOUNT, result.getBalanceType());
        assertEquals(balanceEntity.getAccountId(), result.getAccountId());
        assertEquals(balanceEntity.getBalance(), result.getBalance());

        verify(balanceRepository).balanceQuery(any(BalanceQo.class));
    }

    /**
     * 测试查询余额不存在异常
     */
    @Test
    @DisplayName("测试查询余额不存在异常")
    void testQuery_NotFound() {
        // Given
        when(balanceRepository.balanceQuery(any(BalanceQo.class))).thenReturn(null);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> balanceService.query(queryDto));
        assertEquals("查询余额信息失败，余额信息不存在", exception.getMessage());

        verify(balanceRepository).balanceQuery(any(BalanceQo.class));
    }

    /**
     * 测试电表余额查询场景
     */
    @Test
    @DisplayName("测试电表余额查询场景")
    void testQuery_ElectricMeter() {
        // Given
        queryDto.setBalanceType(BalanceTypeEnum.ELECTRIC_METER);
        balanceEntity.setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode());
        when(balanceRepository.balanceQuery(any(BalanceQo.class))).thenReturn(balanceEntity);

        // When
        BalanceBo result = balanceService.query(queryDto);

        // Then
        assertNotNull(result);
        assertEquals(BalanceTypeEnum.ELECTRIC_METER, result.getBalanceType());

        verify(balanceRepository).balanceQuery(any(BalanceQo.class));
    }

    /**
     * 测试正常初始化账户余额场景
     */
    @Test
    @DisplayName("测试正常初始化账户余额场景")
    void testInitAccountBalance_Normal() {
        // Given
        Integer accountId = 1;
        when(balanceRepository.insert(any(BalanceEntity.class))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> balanceService.initAccountBalance(accountId));

        // Then
        verify(balanceRepository).insert(ArgumentMatchers.<BalanceEntity>argThat(entity ->
                entity.getBalanceRelationId().equals(accountId) &&
                entity.getBalanceType().equals(BalanceTypeEnum.ACCOUNT.getCode()) &&
                entity.getBalance().equals(BigDecimal.ZERO) &&
                entity.getAccountId().equals(accountId)
        ));
    }

    /**
     * 测试初始化账户余额重复异常
     */
    @Test
    @DisplayName("测试初始化账户余额重复异常")
    void testInitAccountBalance_Duplicate() {
        // Given
        Integer accountId = 1;
        when(balanceRepository.insert(any(BalanceEntity.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.initAccountBalance(accountId));
        assertEquals("账户余额初始化异常，账户已存在，请修复数据", exception.getMessage());

        verify(balanceRepository).insert(any(BalanceEntity.class));
    }

    /**
     * 测试正常初始化电表余额场景
     */
    @Test
    @DisplayName("测试正常初始化电表余额场景")
    void testInitElectricMeterBalance_Normal() {
        // Given
        Integer electricMeterId = 2;
        Integer accountId = 1;
        when(balanceRepository.insert(any(BalanceEntity.class))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> balanceService.initElectricMeterBalance(electricMeterId, accountId));

        // Then
        verify(balanceRepository).insert(ArgumentMatchers.<BalanceEntity>argThat(entity ->
                entity.getBalanceRelationId().equals(electricMeterId) &&
                entity.getBalanceType().equals(BalanceTypeEnum.ELECTRIC_METER.getCode()) &&
                entity.getBalance().equals(BigDecimal.ZERO) &&
                entity.getAccountId().equals(accountId)
        ));
    }

    /**
     * 测试初始化电表余额重复异常
     */
    @Test
    @DisplayName("测试初始化电表余额重复异常")
    void testInitElectricMeterBalance_Duplicate() {
        // Given
        Integer electricMeterId = 2;
        Integer accountId = 1;
        when(balanceRepository.insert(any(BalanceEntity.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.initElectricMeterBalance(electricMeterId, accountId));
        assertEquals("账户余额初始化异常，账户已存在，请修复数据", exception.getMessage());

        verify(balanceRepository).insert(any(BalanceEntity.class));
    }

    /**
     * 测试不同余额类型的初始化
     */
    @Test
    @DisplayName("测试不同余额类型的初始化")
    void testInitBalance_DifferentTypes() {
        // Given
        Integer accountId = 1;
        Integer electricMeterId = 2;
        when(balanceRepository.insert(any(BalanceEntity.class))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> {
            balanceService.initAccountBalance(accountId);
            balanceService.initElectricMeterBalance(electricMeterId, accountId);
        });

        // Then
        verify(balanceRepository, times(2)).insert(any(BalanceEntity.class));

        // 验证账户余额初始化
        verify(balanceRepository).insert(ArgumentMatchers.<BalanceEntity>argThat(entity ->
                entity.getBalanceType().equals(BalanceTypeEnum.ACCOUNT.getCode())
        ));

        // 验证电表余额初始化
        verify(balanceRepository).insert(ArgumentMatchers.<BalanceEntity>argThat(entity ->
                entity.getBalanceType().equals(BalanceTypeEnum.ELECTRIC_METER.getCode())
        ));
    }

    /**
     * 测试正常删除余额场景
     */
    @Test
    @DisplayName("测试正常删除余额场景")
    void testDeleteBalance_Normal() {
        // Given
        when(balanceRepository.deleteBalance(any(BalanceQo.class))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> balanceService.deleteBalance(deleteDto));

        // Then
        verify(balanceRepository).deleteBalance(ArgumentMatchers.argThat(qo ->
                qo.getBalanceRelationId().equals(deleteDto.getBalanceRelationId()) &&
                qo.getBalanceType().equals(deleteDto.getBalanceType().getCode())
        ));
    }

    /**
     * 测试删除不存在记录异常场景
     */
    @Test
    @DisplayName("测试删除不存在记录异常场景")
    void testDeleteBalance_RecordNotFound() {
        // Given
        when(balanceRepository.deleteBalance(any(BalanceQo.class))).thenReturn(0);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.deleteBalance(deleteDto));
        assertEquals("账户余额删除异常，请重试", exception.getMessage());

        verify(balanceRepository).deleteBalance(any(BalanceQo.class));
    }

    /**
     * 测试删除电表余额类型场景
     */
    @Test
    @DisplayName("测试删除电表余额类型场景")
    void testDeleteBalance_ElectricMeterType() {
        // Given
        BalanceDeleteDto electricMeterDeleteDto = new BalanceDeleteDto();
        electricMeterDeleteDto.setBalanceRelationId(123);
        electricMeterDeleteDto.setBalanceType(BalanceTypeEnum.ELECTRIC_METER);

        when(balanceRepository.deleteBalance(any(BalanceQo.class))).thenReturn(1);

        // When
        balanceService.deleteBalance(electricMeterDeleteDto);

        // Then
        verify(balanceRepository).deleteBalance(ArgumentMatchers.argThat(qo ->
                qo.getBalanceRelationId().equals(electricMeterDeleteDto.getBalanceRelationId()) &&
                qo.getBalanceType().equals(electricMeterDeleteDto.getBalanceType().getCode())
        ));
    }

    /**
     * 测试删除用户余额类型场景
     */
    @Test
    @DisplayName("测试删除用户余额类型场景")
    void testDeleteBalance_UserType() {
        // Given
        BalanceDeleteDto userDeleteDto = new BalanceDeleteDto();
        userDeleteDto.setBalanceRelationId(456);
        userDeleteDto.setBalanceType(BalanceTypeEnum.ACCOUNT);

        when(balanceRepository.deleteBalance(any(BalanceQo.class))).thenReturn(1);

        // When
        balanceService.deleteBalance(userDeleteDto);

        // Then
        verify(balanceRepository).deleteBalance(ArgumentMatchers.argThat(qo ->
                qo.getBalanceRelationId().equals(userDeleteDto.getBalanceRelationId()) &&
                qo.getBalanceType().equals(userDeleteDto.getBalanceType().getCode())
        ));
    }

    /**
     * 测试正常扣费场景
     */
    @Test
    @DisplayName("测试正常扣费场景")
    void testDeduct_Normal() {
        // Given
        when(orderFlowRepository.insert(any(OrderFlowEntity.class))).thenReturn(1);
        when(balanceRepository.balanceTopUp(any(BalanceQo.class))).thenReturn(1);
        when(balanceRepository.balanceQuery(any(BalanceQo.class))).thenReturn(new BalanceEntity().setBalance(new BigDecimal("12.00")));

        // When
        assertDoesNotThrow(() -> balanceService.deduct(deductDto));

        // Then
        verify(orderFlowRepository).insert(ArgumentMatchers.<OrderFlowEntity>argThat(entity ->
                entity.getConsumeId().equals(deductDto.getOrderNo()) &&
                entity.getAmount().equals(deductDto.getAmount().negate()) && // 验证金额被转换为负数
                entity.getBalanceRelationId().equals(deductDto.getBalanceRelationId()) &&
                entity.getBalanceType().equals(deductDto.getBalanceType().getCode()) &&
                entity.getAccountId().equals(deductDto.getAccountId())
        ));
        verify(balanceRepository).balanceTopUp(ArgumentMatchers.argThat(qo ->
                qo.getBalanceRelationId().equals(deductDto.getBalanceRelationId()) &&
                qo.getBalanceType().equals(deductDto.getBalanceType().getCode()) &&
                qo.getAccountId().equals(deductDto.getAccountId()) &&
                qo.getAmount().equals(deductDto.getAmount().negate()) // 验证金额被转换为负数
        ));
    }

    /**
     * 测试重复订单扣费异常
     */
    @Test
    @DisplayName("测试重复订单扣费异常")
    void testDeduct_DuplicateOrder() {
        // Given
        when(orderFlowRepository.insert(any(OrderFlowEntity.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.deduct(deductDto));
        assertEquals("订单不能重复操作，订单号：DEDUCT123456", exception.getMessage());

        verify(orderFlowRepository).insert(any(OrderFlowEntity.class));
        verify(balanceRepository, never()).balanceTopUp(any(BalanceQo.class));
    }

    /**
     * 测试余额更新失败异常
     */
    @Test
    @DisplayName("测试余额更新失败异常")
    void testDeduct_BalanceUpdateFailed() {
        // Given
        when(orderFlowRepository.insert(any(OrderFlowEntity.class))).thenReturn(1);
        when(balanceRepository.balanceTopUp(any(BalanceQo.class))).thenReturn(0);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.deduct(deductDto));
        assertEquals("账户余额结算异常，请重试", exception.getMessage());

        verify(orderFlowRepository).insert(any(OrderFlowEntity.class));
        verify(balanceRepository).balanceTopUp(any(BalanceQo.class));
    }

    /**
     * 测试余额更新返回null异常
     */
    @Test
    @DisplayName("测试余额更新返回null异常")
    void testDeduct_BalanceUpdateReturnNull() {
        // Given
        when(orderFlowRepository.insert(any(OrderFlowEntity.class))).thenReturn(1);
        when(balanceRepository.balanceTopUp(any(BalanceQo.class))).thenReturn(null);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> balanceService.deduct(deductDto));
        assertEquals("账户余额结算异常，请重试", exception.getMessage());

        verify(orderFlowRepository).insert(any(OrderFlowEntity.class));
        verify(balanceRepository).balanceTopUp(any(BalanceQo.class));
    }

    /**
     * 测试电表余额扣费场景
     */
    @Test
    @DisplayName("测试电表余额扣费场景")
    void testDeduct_ElectricMeterType() {
        // Given
        BalanceDto electricMeterDeductDto = new BalanceDto()
                .setOrderNo("DEDUCT_METER123")
                .setAmount(BigDecimal.valueOf(30.00))
                .setBalanceRelationId(2)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setAccountId(1);

        when(orderFlowRepository.insert(any(OrderFlowEntity.class))).thenReturn(1);
        when(balanceRepository.balanceTopUp(any(BalanceQo.class))).thenReturn(1);
        when(balanceRepository.balanceQuery(any(BalanceQo.class))).thenReturn(new BalanceEntity().setBalance(new BigDecimal("12.00")));

        // When
        assertDoesNotThrow(() -> balanceService.deduct(electricMeterDeductDto));

        // Then
        verify(orderFlowRepository).insert(ArgumentMatchers.<OrderFlowEntity>argThat(entity ->
                entity.getConsumeId().equals(electricMeterDeductDto.getOrderNo()) &&
                entity.getAmount().equals(electricMeterDeductDto.getAmount().negate()) &&
                entity.getBalanceRelationId().equals(electricMeterDeductDto.getBalanceRelationId()) &&
                entity.getBalanceType().equals(BalanceTypeEnum.ELECTRIC_METER.getCode()) &&
                entity.getAccountId().equals(electricMeterDeductDto.getAccountId())
        ));
        verify(balanceRepository).balanceTopUp(ArgumentMatchers.argThat(qo ->
                qo.getBalanceRelationId().equals(electricMeterDeductDto.getBalanceRelationId()) &&
                qo.getBalanceType().equals(BalanceTypeEnum.ELECTRIC_METER.getCode()) &&
                qo.getAccountId().equals(electricMeterDeductDto.getAccountId()) &&
                qo.getAmount().equals(electricMeterDeductDto.getAmount().negate())
        ));
    }
}
