package info.zhihui.ems.mq.rabbit.order;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.business.finance.entity.BalanceEntity;
import info.zhihui.ems.business.finance.qo.BalanceListQueryQo;
import info.zhihui.ems.business.finance.repository.BalanceRepository;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.message.order.status.TerminationSuccessMessage;
import info.zhihui.ems.mq.rabbitmq.entity.TransactionMessageEntity;
import info.zhihui.ems.mq.rabbitmq.listener.order.success.TerminationListener;
import info.zhihui.ems.mq.rabbitmq.repository.TransactionMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TerminationListener 集成测试。
 * 验证订单终止成功消息能够驱动账户销户、余额删除和事务消息标记。
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
class TerminationListenerIntegrationTest {

    private static final String BUSINESS_TYPE_ORDER = TransactionMessageBusinessTypeEnum.ORDER_PAYMENT.name();

    @Autowired
    private TerminationListener terminationListener;

    @Autowired
    private ElectricMeterRepository electricMeterRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private TransactionMessageRepository transactionMessageRepository;

    @Test
    @DisplayName("按需计费账户销户成功测试 - 每个电表单独销户")
    @Transactional
    void handleTerminationSuccess_QuantityAccount_Success() {
        // 准备测试数据
        List<ElectricMeterEntity> meters = selectPreparedMeters(2);
        String orderSn = "IT-TERMINATION-QUANTITY-" + System.nanoTime();

        // 为每个电表创建余额记录
        for (ElectricMeterEntity meter : meters) {
            prepareBalanceForMeter(meter);
        }
        prepareTransactionMessage(orderSn);

        // 构造消息
        TerminationSuccessMessage message = new TerminationSuccessMessage();
        message.setAccountId(meters.get(0).getAccountId())
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricMeterAmount(meters.size())
                .setFullCancel(true)
                .setMeterIdList(meters.stream().map(ElectricMeterEntity::getId).toList())
                .setOrderSn(orderSn)
                .setOrderStatus("SUCCESS");

        // 执行处理
        terminationListener.handleTerminationSuccess(message);

        // 验证每个电表的余额都被删除
        for (ElectricMeterEntity meter : meters) {
            BalanceEntity balance = queryBalance(meter);
            assertThat(balance).isNull();
        }

        // 验证事务消息标记成功
        TransactionMessageEntity transactionMessage = transactionMessageRepository.getByBusinessTypeAndSn(BUSINESS_TYPE_ORDER, orderSn);
        assertThat(transactionMessage).isNotNull();
        assertThat(transactionMessage.getIsSuccess()).isTrue();
    }

    @Test
    @DisplayName("包月计费账户fullCancel=true时销户成功测试")
    @Transactional
    void handleTerminationSuccess_MonthlyAccount_FullCancel_Success() {
        // 准备测试数据
        List<ElectricMeterEntity> meters = selectPreparedMeters(2);
        String orderSn = "IT-TERMINATION-MONTHLY-" + System.nanoTime();

        // 为账户创建余额记录
        prepareBalanceForAccount(meters.get(0).getAccountId());
        prepareTransactionMessage(orderSn);

        // 构造消息
        TerminationSuccessMessage message = new TerminationSuccessMessage();
        message.setAccountId(meters.get(0).getAccountId())
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setElectricMeterAmount(meters.size())
                .setFullCancel(true)
                .setMeterIdList(meters.stream().map(ElectricMeterEntity::getId).toList())
                .setOrderSn(orderSn)
                .setOrderStatus("SUCCESS");

        // 执行处理
        terminationListener.handleTerminationSuccess(message);

        // 验证账户余额被删除
        BalanceEntity accountBalance = queryAccountBalance(meters.get(0).getAccountId());
        assertThat(accountBalance).isNull();

        // 验证事务消息标记成功
        TransactionMessageEntity transactionMessage = transactionMessageRepository.getByBusinessTypeAndSn(BUSINESS_TYPE_ORDER, orderSn);
        assertThat(transactionMessage).isNotNull();
        assertThat(transactionMessage.getIsSuccess()).isTrue();
    }

    @Test
    @DisplayName("合并计费账户fullCancel=true时销户成功测试")
    @Transactional
    void handleTerminationSuccess_MergedAccount_FullCancel_Success() {
        // 准备测试数据
        List<ElectricMeterEntity> meters = selectPreparedMeters(2);
        String orderSn = "IT-TERMINATION-MERGED-" + System.nanoTime();

        // 为账户创建余额记录
        prepareBalanceForAccount(meters.get(0).getAccountId());
        prepareTransactionMessage(orderSn);

        // 构造消息
        TerminationSuccessMessage message = new TerminationSuccessMessage();
        message.setAccountId(meters.get(0).getAccountId())
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setElectricMeterAmount(meters.size())
                .setFullCancel(true)
                .setMeterIdList(meters.stream().map(ElectricMeterEntity::getId).toList())
                .setOrderSn(orderSn)
                .setOrderStatus("SUCCESS");

        // 执行处理
        terminationListener.handleTerminationSuccess(message);

        // 验证账户余额被删除
        BalanceEntity accountBalance = queryAccountBalance(meters.get(0).getAccountId());
        assertThat(accountBalance).isNull();

        // 验证事务消息标记成功
        TransactionMessageEntity transactionMessage = transactionMessageRepository.getByBusinessTypeAndSn(BUSINESS_TYPE_ORDER, orderSn);
        assertThat(transactionMessage).isNotNull();
        assertThat(transactionMessage.getIsSuccess()).isTrue();
    }

    @Test
    @DisplayName("包月计费账户fullCancel=false时跳过销户测试")
    @Transactional
    void handleTerminationSuccess_MonthlyAccount_NotFullCancel_Skip() {
        // 准备测试数据
        List<ElectricMeterEntity> meters = selectPreparedMeters(1);
        String orderSn = "IT-TERMINATION-MONTHLY-SKIP-" + System.nanoTime();

        // 为账户创建余额记录
        BigDecimal initialBalance = new BigDecimal("3400.50");
        prepareTransactionMessage(orderSn);

        // 构造消息
        TerminationSuccessMessage message = new TerminationSuccessMessage();
        message.setAccountId(meters.get(0).getAccountId())
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setElectricMeterAmount(meters.size())
                .setFullCancel(false)
                .setMeterIdList(meters.stream().map(ElectricMeterEntity::getId).toList())
                .setOrderSn(orderSn)
                .setOrderStatus("SUCCESS");

        // 执行处理
        terminationListener.handleTerminationSuccess(message);

        // 验证账户余额未被删除
        BalanceEntity accountBalance = queryAccountBalance(meters.get(0).getAccountId());
        assertThat(accountBalance).isNotNull();
        assertThat(accountBalance.getBalance()).isEqualByComparingTo(initialBalance);

        // 验证事务消息标记成功
        TransactionMessageEntity transactionMessage = transactionMessageRepository.getByBusinessTypeAndSn(BUSINESS_TYPE_ORDER, orderSn);
        assertThat(transactionMessage).isNotNull();
        assertThat(transactionMessage.getIsSuccess()).isTrue();
    }

    @Test
    @DisplayName("参数校验失败测试 - 电表数量不一致")
    @Transactional
    void handleTerminationSuccess_ValidationFailed_MeterCountMismatch() {
        // 准备测试数据
        List<ElectricMeterEntity> meters = selectPreparedMeters(2);
        String orderSn = "IT-TERMINATION-VALIDATION-" + System.nanoTime();
        prepareTransactionMessage(orderSn);

        // 构造消息 - 电表数量与实际不符
        TerminationSuccessMessage message = new TerminationSuccessMessage();
        message.setAccountId(meters.get(0).getAccountId())
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricMeterAmount(3) // 实际只有2个电表
                .setFullCancel(true)
                .setMeterIdList(meters.stream().map(ElectricMeterEntity::getId).toList())
                .setOrderSn(orderSn)
                .setOrderStatus("SUCCESS");

        // 执行处理 - 应该抛出异常
        try {
            terminationListener.handleTerminationSuccess(message);
        } catch (Exception e) {
            // 预期会抛出异常
        }

        // 验证事务消息标记失败
        TransactionMessageEntity transactionMessage = transactionMessageRepository.getByBusinessTypeAndSn(BUSINESS_TYPE_ORDER, orderSn);
        assertThat(transactionMessage).isNotNull();
        assertThat(transactionMessage.getIsSuccess()).isFalse();
    }

    // 辅助方法
    private List<ElectricMeterEntity> selectPreparedMeters(int count) {
        List<ElectricMeterEntity> meters = electricMeterRepository.selectList(
                new QueryWrapper<ElectricMeterEntity>()
                        .eq("is_deleted", false)
                        .isNotNull("account_id")
                        .last("limit " + count)
        );
        if (meters.size() < count) {
            throw new RuntimeException("测试数据中缺少足够的电表记录，需要：" + count + "，实际：" + meters.size());
        }
        return meters;
    }

    private void prepareBalanceForMeter(ElectricMeterEntity meter) {
        prepareBalanceForMeter(meter, new BigDecimal("50.00"));
    }

    private void prepareBalanceForMeter(ElectricMeterEntity meter, BigDecimal balance) {
        BalanceEntity balanceEntity = new BalanceEntity()
                .setBalanceRelationId(meter.getId())
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setAccountId(meter.getAccountId())
                .setBalance(balance);
        balanceRepository.insert(balanceEntity);
    }

    private void prepareBalanceForAccount(Integer accountId) {
        prepareBalanceForAccount(accountId, new BigDecimal("200.00"));
    }

    private void prepareBalanceForAccount(Integer accountId, BigDecimal balance) {
        BalanceEntity balanceEntity = new BalanceEntity()
                .setBalanceRelationId(accountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode())
                .setAccountId(accountId)
                .setBalance(balance);
        balanceRepository.updateById(balanceEntity);
    }

    private void prepareTransactionMessage(String orderSn) {
        TransactionMessageEntity entity = new TransactionMessageEntity()
                .setBusinessType(BUSINESS_TYPE_ORDER)
                .setSn(orderSn)
                .setDestination("order-topic")
                .setRoute("order-success")
                .setPayloadType(String.class.getName())
                .setPayload("{}")
                .setCreateTime(LocalDateTime.now())
                .setTryTimes(0)
                .setIsSuccess(false);
        transactionMessageRepository.insert(entity);
    }

    private BalanceEntity queryBalance(ElectricMeterEntity meter) {
        return balanceRepository.findListByQuery(new BalanceListQueryQo()
                        .setAccountIds(List.of(meter.getAccountId()))
                        .setBalanceRelationIds(List.of(meter.getId()))
                        .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode()))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private BalanceEntity queryAccountBalance(Integer accountId) {
        return balanceRepository.findListByQuery(new BalanceListQueryQo()
                        .setAccountIds(List.of(accountId))
                        .setBalanceRelationIds(List.of(accountId))
                        .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode()))
                .stream()
                .findFirst()
                .orElse(null);
    }
}
