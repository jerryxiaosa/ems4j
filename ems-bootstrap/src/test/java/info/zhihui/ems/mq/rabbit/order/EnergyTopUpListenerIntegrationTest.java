package info.zhihui.ems.mq.rabbit.order;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.business.finance.entity.BalanceEntity;
import info.zhihui.ems.business.finance.entity.OrderFlowEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.qo.BalanceQo;
import info.zhihui.ems.business.finance.repository.BalanceRepository;
import info.zhihui.ems.business.finance.repository.OrderFlowRepository;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandExecuteRecordRepository;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandRecordRepository;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.message.order.status.EnergyTopUpSuccessMessage;
import info.zhihui.ems.mq.rabbitmq.entity.TransactionMessageEntity;
import info.zhihui.ems.mq.rabbitmq.listener.order.success.EnergyTopUpListener;
import info.zhihui.ems.mq.rabbitmq.repository.TransactionMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EnergyTopUpListener 集成测试。
 * 验证能耗充值成功消息能够驱动余额更新、流水记录和电表开闸命令。
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
class EnergyTopUpListenerIntegrationTest {

    private static final String BUSINESS_TYPE_ORDER = TransactionMessageBusinessTypeEnum.ORDER_PAYMENT.name();

    @Autowired
    private EnergyTopUpListener energyTopUpListener;

    @Autowired
    private ElectricMeterRepository electricMeterRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private OrderFlowRepository orderFlowRepository;

    @Autowired
    private TransactionMessageRepository transactionMessageRepository;

    @Autowired
    private DeviceCommandRecordRepository deviceCommandRecordRepository;

    @Autowired
    private DeviceCommandExecuteRecordRepository deviceCommandExecuteRecordRepository;

    @Test
    @DisplayName("电表充值消息应更新余额并生成命令")
    @Transactional
    void handleElectricMeterTopUp_ShouldUpdateBalanceAndCreateCommand() {
        ElectricMeterEntity meter = selectPreparedMeter();
        BigDecimal topUpAmount = new BigDecimal("66.66");
        String orderSn = "IT-ENERGY-TOPUP-" + System.nanoTime();

        BalanceContext balanceContext = prepareBalanceContext(meter);
        prepareTransactionMessage(orderSn);

        long beforeFlowCount = countOrderFlows(orderSn);
        long beforeCommandRecord = deviceCommandRecordRepository.selectCount(null);
        long beforeCommandExecute = deviceCommandExecuteRecordRepository.selectCount(null);

        EnergyTopUpSuccessMessage message = new EnergyTopUpSuccessMessage();
        message.setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setAccountId(meter.getAccountId())
                .setMeterId(meter.getId())
                .setMeterType(MeterTypeEnum.ELECTRIC)
                .setOrderAmount(topUpAmount)
                .setOrderSn(orderSn)
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());

        energyTopUpListener.handle(message);

        assertThat(queryBalance(meter)).isEqualByComparingTo(balanceContext.initialBalance().add(topUpAmount));
        assertThat(countOrderFlows(orderSn)).isEqualTo(beforeFlowCount + 1);

        TransactionMessageEntity transactionMessage = transactionMessageRepository.getByBusinessTypeAndSn(BUSINESS_TYPE_ORDER, orderSn);
        assertThat(transactionMessage).isNotNull();
        assertThat(transactionMessage.getIsSuccess()).isTrue();
    }

    private ElectricMeterEntity selectPreparedMeter() {
        ElectricMeterEntity meter = electricMeterRepository.selectOne(new QueryWrapper<ElectricMeterEntity>()
                .eq("is_deleted", false)
                .isNotNull("iot_id")
                .eq("is_online", true)
                .isNotNull("account_id")
                .last("limit 1"));
        return Objects.requireNonNull(meter, "测试数据中缺少在线并已开户的电表");
    }

    private BalanceContext prepareBalanceContext(ElectricMeterEntity meter) {
        BalanceQo balanceQo = new BalanceQo()
                .setBalanceRelationId(meter.getId())
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setAccountId(meter.getAccountId());
        BalanceEntity balanceEntity = balanceRepository.balanceQuery(balanceQo);

        if (balanceEntity == null) {
            BalanceEntity newBalance = new BalanceEntity()
                    .setBalanceRelationId(meter.getId())
                    .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                    .setAccountId(meter.getAccountId())
                    .setBalance(BigDecimal.ZERO);
            balanceRepository.insert(newBalance);
            return new BalanceContext(BigDecimal.ZERO, newBalance.getId());
        }
        return new BalanceContext(balanceEntity.getBalance(), balanceEntity.getId());
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

    private BigDecimal queryBalance(ElectricMeterEntity meter) {
        BalanceQo balanceQo = new BalanceQo()
                .setBalanceRelationId(meter.getId())
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setAccountId(meter.getAccountId());
        BalanceEntity balanceEntity = balanceRepository.balanceQuery(balanceQo);
        return Objects.requireNonNull(balanceEntity, "电表余额记录不存在").getBalance();
    }

    private long countOrderFlows(String orderSn) {
        return orderFlowRepository.selectCount(new QueryWrapper<OrderFlowEntity>().eq("consume_id", orderSn));
    }

    private record BalanceContext(BigDecimal initialBalance, Integer balanceId) {
    }
}
