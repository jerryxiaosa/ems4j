package info.zhihui.ems.business.finance.balance;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceDeleteDto;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.BalanceDto;
import info.zhihui.ems.business.finance.entity.OrderFlowEntity;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.business.finance.repository.OrderFlowRepository;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BalanceServiceImpl 集成测试
 *
 * @author jerryxiaosa
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("integrationtest")
class BalanceServiceImplIntegrationTest {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private OrderFlowRepository orderFlowRepository;

    private BalanceDto testTopUpDto;
    private BalanceDto testDeductDto;
    private BalanceQueryDto testQueryDto;
    private BalanceDeleteDto testDeleteDto;
    private Integer testAccountId;
    private Integer testElectricMeterId;

    @BeforeEach
    void setUp() {
        testAccountId = 1001;
        testElectricMeterId = 2001;

        // 初始化测试充值DTO
        testTopUpDto = new BalanceDto()
                .setOrderNo("ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("100.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        // 初始化测试扣费DTO
        testDeductDto = new BalanceDto()
                .setOrderNo("DEDUCT_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("30.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        // 初始化测试查询DTO
        testQueryDto = new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(testAccountId);

        // 初始化测试删除DTO
        testDeleteDto = new BalanceDeleteDto()
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT);

         try {
             balanceService.initAccountBalance(testAccountId);
         } catch (BusinessRuntimeException e) {
             // 如果已存在则忽略
             if (!e.getMessage().contains("账户已存在")) {
                 throw e;
             }
         }
    }

    @Test
    @DisplayName("正常充值测试 - 应成功充值并更新余额")
    @Transactional
    void testTopUp_NormalTopUp_ShouldSuccess() {
        // 执行充值
        assertDoesNotThrow(() -> {
            balanceService.topUp(testTopUpDto);
        });

        // 验证订单流水记录已保存
        List<OrderFlowEntity> orderFlows = orderFlowRepository.selectList(null);
        OrderFlowEntity savedOrderFlow = orderFlows.stream()
                .filter(flow -> flow.getConsumeId().equals(testTopUpDto.getOrderNo()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedOrderFlow, "应该保存订单流水记录");
        assertEquals(testTopUpDto.getAmount(), savedOrderFlow.getAmount().setScale(2, RoundingMode.FLOOR));
        assertEquals(testTopUpDto.getBalanceRelationId(), savedOrderFlow.getBalanceRelationId());
        assertEquals(testTopUpDto.getBalanceType().getCode(), savedOrderFlow.getBalanceType());
        assertEquals(testTopUpDto.getAccountId(), savedOrderFlow.getAccountId());

        // 验证余额已更新
        BalanceBo balance = balanceService.query(testQueryDto);
        assertEquals(testTopUpDto.getAmount(), balance.getBalance().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("重复充值测试 - 应抛出业务异常")
    @Transactional
    void testTopUp_DuplicateOrder_ShouldThrowException() {
        // 第一次充值
        balanceService.topUp(testTopUpDto);

        // 第二次使用相同订单号充值
        BalanceDto duplicateDto = new BalanceDto()
                .setOrderNo(testTopUpDto.getOrderNo())
                .setAmount(new BigDecimal("50.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            balanceService.topUp(duplicateDto);
        });

        assertTrue(exception.getMessage().startsWith("订单不能重复操作，订单号"));

        // 验证余额没有重复增加
        BalanceBo balance = balanceService.query(testQueryDto);
        assertEquals(testTopUpDto.getAmount(), balance.getBalance().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("多次充值测试 - 应累计余额")
    @Transactional
    void testTopUp_MultipleTopUp_ShouldAccumulateBalance() {
        // 第一次充值
        balanceService.topUp(testTopUpDto);

        // 第二次充值
        BalanceDto secondTopUpDto = new BalanceDto()
                .setOrderNo("ORDER_SECOND_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("50.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        balanceService.topUp(secondTopUpDto);

        // 验证余额累计
        BalanceBo balance = balanceService.query(testQueryDto);
        BigDecimal expectedBalance = testTopUpDto.getAmount().add(secondTopUpDto.getAmount());
        assertEquals(expectedBalance, balance.getBalance().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("查询余额测试 - 正常查询应返回余额信息")
    @Transactional
    void testQuery_NormalQuery_ShouldReturnBalance() {
        // 先充值
        balanceService.topUp(testTopUpDto);

        // 查询余额
        BalanceBo balance = balanceService.query(testQueryDto);

        assertNotNull(balance);
        assertEquals(testAccountId, balance.getBalanceRelationId());
        assertEquals(BalanceTypeEnum.ACCOUNT, balance.getBalanceType());
        assertEquals(testAccountId, balance.getAccountId());
        assertEquals(testTopUpDto.getAmount(), balance.getBalance().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("查询不存在余额测试 - 应抛出NotFoundException")
    @Transactional
    void testQuery_NonExistentBalance_ShouldThrowNotFoundException() {
        BalanceQueryDto nonExistentQueryDto = new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(99999); // 不存在的关联ID

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            balanceService.query(nonExistentQueryDto);
        });

        assertEquals("查询余额信息失败，余额信息不存在", exception.getMessage());
    }



    @Test
    @DisplayName("正常扣费测试 - 应成功扣费并更新余额")
    @Transactional
    void testDeduct_NormalDeduct_ShouldSuccess() {
        // 先充值创建余额
        balanceService.topUp(testTopUpDto);

        // 验证充值后余额
        BalanceBo balanceBeforeDeduct = balanceService.query(testQueryDto);
        assertEquals(testTopUpDto.getAmount(), balanceBeforeDeduct.getBalance().setScale(2, RoundingMode.FLOOR));

        // 执行扣费
        assertDoesNotThrow(() -> {
            balanceService.deduct(testDeductDto);
        });

        // 验证订单流水记录已保存，且金额为负数
        List<OrderFlowEntity> orderFlows = orderFlowRepository.selectList(null);
        OrderFlowEntity savedOrderFlow = orderFlows.stream()
                .filter(flow -> flow.getConsumeId().equals(testDeductDto.getOrderNo()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedOrderFlow, "应该保存订单流水记录");
        assertEquals(testDeductDto.getAmount().negate(), savedOrderFlow.getAmount().setScale(2, RoundingMode.FLOOR), "扣费流水记录金额应为负数");
        assertEquals(testDeductDto.getBalanceRelationId(), savedOrderFlow.getBalanceRelationId());
        assertEquals(testDeductDto.getBalanceType().getCode(), savedOrderFlow.getBalanceType());
        assertEquals(testDeductDto.getAccountId(), savedOrderFlow.getAccountId());

        // 验证余额已正确减少
        BalanceBo balanceAfterDeduct = balanceService.query(testQueryDto);
        BigDecimal expectedBalance = testTopUpDto.getAmount().subtract(testDeductDto.getAmount());
        assertEquals(expectedBalance, balanceAfterDeduct.getBalance().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("重复订单扣费测试 - 应抛出业务异常")
    @Transactional
    void testDeduct_DuplicateOrder_ShouldThrowException() {
        // 先充值创建余额
        balanceService.topUp(testTopUpDto);

        // 第一次扣费
        balanceService.deduct(testDeductDto);

        // 第二次使用相同订单号扣费
        BalanceDto duplicateDeductDto = new BalanceDto()
                .setOrderNo(testDeductDto.getOrderNo())
                .setAmount(new BigDecimal("20.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            balanceService.deduct(duplicateDeductDto);
        });

        assertTrue(exception.getMessage().startsWith("订单不能重复操作，订单号"));

        // 验证余额没有重复扣费
        BalanceBo balance = balanceService.query(testQueryDto);
        BigDecimal expectedBalance = testTopUpDto.getAmount().subtract(testDeductDto.getAmount());
        assertEquals(expectedBalance, balance.getBalance().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("余额不足扣费测试 - 应正确处理余额不足情况")
    @Transactional
    void testDeduct_InsufficientBalance_ShouldHandleCorrectly() {
        // 先充值少量余额
        BalanceDto smallTopUpDto = new BalanceDto()
                .setOrderNo("SMALL_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("10.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        balanceService.topUp(smallTopUpDto);

        // 尝试扣费超过余额的金额
        BalanceDto largeDeductDto = new BalanceDto()
                .setOrderNo("LARGE_DEDUCT_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("50.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        // 执行扣费，应该成功（允许负余额）
        assertDoesNotThrow(() -> {
            balanceService.deduct(largeDeductDto);
        });

        // 验证余额变为负数
        BalanceBo balance = balanceService.query(testQueryDto);
        BigDecimal expectedBalance = smallTopUpDto.getAmount().subtract(largeDeductDto.getAmount());
        assertEquals(expectedBalance, balance.getBalance().setScale(2, RoundingMode.FLOOR));
        assertTrue(balance.getBalance().compareTo(BigDecimal.ZERO) < 0, "余额应为负数");
    }

    @Test
    @DisplayName("电表余额扣费测试 - 应成功扣费电表余额")
    @Transactional
    void testDeduct_ElectricMeterBalance_ShouldSuccess() {
        // 初始化电表余额
        balanceService.initElectricMeterBalance(testElectricMeterId, testAccountId);

        // 充值电表余额
        BalanceDto electricMeterTopUpDto = new BalanceDto()
                .setOrderNo("ELECTRIC_TOPUP_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("100.00"))
                .setBalanceRelationId(testElectricMeterId)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setAccountId(testAccountId);

        balanceService.topUp(electricMeterTopUpDto);

        // 扣费电表余额
        BalanceDto electricMeterDeductDto = new BalanceDto()
                .setOrderNo("ELECTRIC_DEDUCT_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("40.00"))
                .setBalanceRelationId(testElectricMeterId)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setAccountId(testAccountId);

        assertDoesNotThrow(() -> {
            balanceService.deduct(electricMeterDeductDto);
        });

        // 验证电表余额正确减少
        BalanceQueryDto electricMeterQueryDto = new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(testElectricMeterId);

        BalanceBo balance = balanceService.query(electricMeterQueryDto);
        BigDecimal expectedBalance = electricMeterTopUpDto.getAmount().subtract(electricMeterDeductDto.getAmount());
        assertEquals(expectedBalance, balance.getBalance().setScale(2, RoundingMode.FLOOR));
        assertEquals(testElectricMeterId, balance.getBalanceRelationId());
        assertEquals(BalanceTypeEnum.ELECTRIC_METER, balance.getBalanceType());
        assertEquals(testAccountId, balance.getAccountId());

        // 验证订单流水记录中金额为负数
        List<OrderFlowEntity> orderFlows = orderFlowRepository.selectList(null);
        OrderFlowEntity savedOrderFlow = orderFlows.stream()
                .filter(flow -> flow.getConsumeId().equals(electricMeterDeductDto.getOrderNo()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedOrderFlow, "应该保存订单流水记录");
        assertEquals(electricMeterDeductDto.getAmount().negate(), savedOrderFlow.getAmount().setScale(2, RoundingMode.FLOOR), "扣费流水记录金额应为负数");
    }

    @Test
    @DisplayName("多次扣费测试 - 应累计扣费金额")
    @Transactional
    void testDeduct_MultipleDeduct_ShouldAccumulateDeduction() {
        // 先充值较大金额
        BalanceDto largeTopUpDto = new BalanceDto()
                .setOrderNo("LARGE_TOPUP_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("200.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        balanceService.topUp(largeTopUpDto);

        // 第一次扣费
        balanceService.deduct(testDeductDto);

        // 第二次扣费
        BalanceDto secondDeductDto = new BalanceDto()
                .setOrderNo("SECOND_DEDUCT_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("25.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        balanceService.deduct(secondDeductDto);

        // 验证余额累计扣费
        BalanceBo balance = balanceService.query(testQueryDto);
        BigDecimal expectedBalance = largeTopUpDto.getAmount()
                .subtract(testDeductDto.getAmount())
                .subtract(secondDeductDto.getAmount());
        assertEquals(expectedBalance, balance.getBalance().setScale(2, RoundingMode.FLOOR));

        // 验证两笔扣费流水记录都存在且金额为负数
        List<OrderFlowEntity> orderFlows = orderFlowRepository.selectList(null);

        OrderFlowEntity firstDeductFlow = orderFlows.stream()
                .filter(flow -> flow.getConsumeId().equals(testDeductDto.getOrderNo()))
                .findFirst()
                .orElse(null);
        assertNotNull(firstDeductFlow, "第一笔扣费流水记录应存在");
        assertEquals(testDeductDto.getAmount().negate(), firstDeductFlow.getAmount().setScale(2, RoundingMode.FLOOR));

        OrderFlowEntity secondDeductFlow = orderFlows.stream()
                .filter(flow -> flow.getConsumeId().equals(secondDeductDto.getOrderNo()))
                .findFirst()
                .orElse(null);
        assertNotNull(secondDeductFlow, "第二笔扣费流水记录应存在");
        assertEquals(secondDeductDto.getAmount().negate(), secondDeductFlow.getAmount().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("查询电表余额测试 - 应正确查询电表余额")
    @Transactional
    void testQuery_ElectricMeterBalance_ShouldReturnCorrectBalance() {
         balanceService.initElectricMeterBalance(testElectricMeterId, testAccountId);

        // 充值电表余额
        BalanceDto electricMeterTopUpDto = new BalanceDto()
                .setOrderNo("ELECTRIC_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("200.00"))
                .setBalanceRelationId(testElectricMeterId)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setAccountId(testAccountId);

        balanceService.topUp(electricMeterTopUpDto);

        // 查询电表余额
        BalanceQueryDto electricMeterQueryDto = new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(testElectricMeterId);

        BalanceBo balance = balanceService.query(electricMeterQueryDto);

        assertNotNull(balance);
        assertEquals(testElectricMeterId, balance.getBalanceRelationId());
        assertEquals(BalanceTypeEnum.ELECTRIC_METER, balance.getBalanceType());
        assertEquals(testAccountId, balance.getAccountId());
        assertEquals(electricMeterTopUpDto.getAmount(), balance.getBalance().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("并发充值测试 - 多线程同时充值应保证数据一致性")
    void testTopUp_ConcurrentTopUp_ShouldMaintainDataConsistency() throws InterruptedException {
        final int threadCount = 5;
        final BigDecimal amountPerThread = new BigDecimal("10.00");
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);

        Integer testAccountId = 200;
        balanceService.initAccountBalance(testAccountId);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 创建多个并发充值任务
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    BalanceDto concurrentTopUpDto = new BalanceDto()
                            .setOrderNo("CONCURRENT_ORDER_" + index + "_" + System.currentTimeMillis())
                            .setAmount(amountPerThread)
                            .setBalanceRelationId(testAccountId)
                            .setBalanceType(BalanceTypeEnum.ACCOUNT)
                            .setAccountId(testAccountId);

                    balanceService.topUp(concurrentTopUpDto);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("并发充值失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        latch.await();
        executor.shutdown();

        // 验证结果
        assertEquals(threadCount, successCount.get(), "所有充值任务都应该成功");

        // 验证最终余额
        BalanceBo finalBalance = balanceService.query(new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(testAccountId));
        BigDecimal expectedBalance = BigDecimal.ZERO.add(amountPerThread.multiply(new BigDecimal(threadCount)));
        assertEquals(expectedBalance, finalBalance.getBalance().setScale(2, RoundingMode.FLOOR), "最终余额应该等于所有充值金额之和");

        // 验证订单流水记录数量
        List<OrderFlowEntity> orderFlows = orderFlowRepository.selectList(null);
        long concurrentOrderCount = orderFlows.stream()
                .filter(flow -> flow.getConsumeId().startsWith("CONCURRENT_ORDER_"))
                .count();
        assertEquals(threadCount, concurrentOrderCount, "应该有对应数量的订单流水记录");
    }

    @Test
    @DisplayName("综合业务流程测试 - 初始化、充值、查询完整流程")
    @Transactional
    void testCompleteBusinessFlow_InitTopUpQuery_ShouldWorkCorrectly() {
        Integer businessAccountId = 7001;
        Integer businessElectricMeterId = 7002;

         balanceService.initAccountBalance(businessAccountId);

         balanceService.initElectricMeterBalance(businessElectricMeterId, businessAccountId);

        // 账户充值
        BalanceDto accountTopUpDto = new BalanceDto()
                .setOrderNo("BUSINESS_ACCOUNT_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("500.00"))
                .setBalanceRelationId(businessAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(businessAccountId);
        balanceService.topUp(accountTopUpDto);

        // 电表充值
        BalanceDto meterTopUpDto = new BalanceDto()
                .setOrderNo("BUSINESS_METER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("300.00"))
                .setBalanceRelationId(businessElectricMeterId)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setAccountId(businessAccountId);
        balanceService.topUp(meterTopUpDto);

        // 查询账户余额
        BalanceQueryDto accountQueryDto = new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(businessAccountId);
        BalanceBo accountBalance = balanceService.query(accountQueryDto);

        // 查询电表余额
        BalanceQueryDto meterQueryDto = new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(businessElectricMeterId);
        BalanceBo meterBalance = balanceService.query(meterQueryDto);

        // 验证结果
        assertEquals(new BigDecimal("500.00"), accountBalance.getBalance().setScale(2, RoundingMode.FLOOR));
        assertEquals(new BigDecimal("300.00"), meterBalance.getBalance().setScale(2, RoundingMode.FLOOR));
        assertEquals(businessAccountId, accountBalance.getAccountId());
        assertEquals(businessAccountId, meterBalance.getAccountId());
    }

    @Test
    @DisplayName("正常删除余额测试 - 应成功删除账户余额")
    @Transactional
    void testDeleteBalance_NormalDelete_ShouldSuccess() {
        // 先充值创建余额记录
        balanceService.topUp(testTopUpDto);

        // 验证余额存在
        BalanceBo balanceBeforeDelete = balanceService.query(testQueryDto);
        assertNotNull(balanceBeforeDelete);
        assertEquals(testTopUpDto.getAmount(), balanceBeforeDelete.getBalance().setScale(2, RoundingMode.FLOOR));

        // 执行删除
        assertDoesNotThrow(() -> {
            balanceService.deleteBalance(testDeleteDto);
        });

        // 验证余额已被删除
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            balanceService.query(testQueryDto);
        });
        assertEquals("查询余额信息失败，余额信息不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除不存在记录测试")
    @Transactional
    void testDeleteBalance_NonExistentRecord_ShouldThrowNotFoundException() {
        BalanceDeleteDto nonExistentDeleteDto = new BalanceDeleteDto()
                .setBalanceRelationId(99999) // 不存在的关联ID
                .setBalanceType(BalanceTypeEnum.ACCOUNT);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            balanceService.deleteBalance(nonExistentDeleteDto);
        });

        assertEquals("账户余额删除异常，请重试", exception.getMessage());
    }

    @Test
    @DisplayName("删除后查询验证测试 - 删除后应无法查询到记录")
    @Transactional
    void testDeleteBalance_QueryAfterDelete_ShouldNotFound() {
        // 先充值创建余额记录
        balanceService.topUp(testTopUpDto);

        // 验证余额存在
        BalanceBo balanceBeforeDelete = balanceService.query(testQueryDto);
        assertNotNull(balanceBeforeDelete);

        // 执行删除
        balanceService.deleteBalance(testDeleteDto);

        // 验证删除后无法查询到记录
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            balanceService.query(testQueryDto);
        });
        assertEquals("查询余额信息失败，余额信息不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除电表余额测试 - 应成功删除电表余额")
    @Transactional
    void testDeleteBalance_ElectricMeterBalance_ShouldSuccess() {
        // 初始化电表余额
        balanceService.initElectricMeterBalance(testElectricMeterId, testAccountId);

        // 充值电表余额
        BalanceDto electricMeterTopUpDto = new BalanceDto()
                .setOrderNo("ELECTRIC_DELETE_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("150.00"))
                .setBalanceRelationId(testElectricMeterId)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setAccountId(testAccountId);

        balanceService.topUp(electricMeterTopUpDto);

        // 验证电表余额存在
        BalanceQueryDto electricMeterQueryDto = new BalanceQueryDto()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(testElectricMeterId);

        BalanceBo balanceBeforeDelete = balanceService.query(electricMeterQueryDto);
        assertNotNull(balanceBeforeDelete);
        assertEquals(electricMeterTopUpDto.getAmount(), balanceBeforeDelete.getBalance().setScale(2, RoundingMode.FLOOR));

        // 创建电表删除DTO
        BalanceDeleteDto electricMeterDeleteDto = new BalanceDeleteDto()
                .setBalanceRelationId(testElectricMeterId)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER);

        // 执行删除
        assertDoesNotThrow(() -> {
            balanceService.deleteBalance(electricMeterDeleteDto);
        });

        // 验证电表余额已被删除
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            balanceService.query(electricMeterQueryDto);
        });
        assertEquals("查询余额信息失败，余额信息不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除多次充值后的余额测试 - 应成功删除累计余额")
    @Transactional
    void testDeleteBalance_MultipleTopUpBalance_ShouldSuccess() {
        // 第一次充值
        balanceService.topUp(testTopUpDto);

        // 第二次充值
        BalanceDto secondTopUpDto = new BalanceDto()
                .setOrderNo("DELETE_SECOND_ORDER_" + System.currentTimeMillis())
                .setAmount(new BigDecimal("75.00"))
                .setBalanceRelationId(testAccountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setAccountId(testAccountId);

        balanceService.topUp(secondTopUpDto);

        // 验证累计余额
        BalanceBo balanceBeforeDelete = balanceService.query(testQueryDto);
        BigDecimal expectedBalance = testTopUpDto.getAmount().add(secondTopUpDto.getAmount());
        assertEquals(expectedBalance, balanceBeforeDelete.getBalance().setScale(2, RoundingMode.FLOOR));

        // 执行删除
        balanceService.deleteBalance(testDeleteDto);

        // 验证余额已被删除
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            balanceService.query(testQueryDto);
        });
        assertEquals("查询余额信息失败，余额信息不存在", exception.getMessage());
    }
}