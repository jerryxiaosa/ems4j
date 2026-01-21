package info.zhihui.ems.mq.rabbitmq.listener.order.success;

import info.zhihui.ems.business.device.dto.ElectricMeterSwitchStatusDto;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.finance.dto.BalanceDto;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.message.order.status.EnergyTopUpSuccessMessage;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 能源充值成功消息监听器
 * 处理订单成功后的充值操作，包括账户充值和电表充值
 *
 * @author jerryxiaosa
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Validated
public class EnergyTopUpListener {

    private final BalanceService balanceService;
    private final ElectricMeterManagerService electricMeterManagerService;
    private final TransactionMessageService transactionMessageService;

    @RabbitListener(queues = QueueConstant.QUEUE_ORDER_SUCCESS_ENERGY_TOP_UP)
    public void handle(@Valid @NotNull EnergyTopUpSuccessMessage message) {
        log.info("接收到能源充值成功消息，订单号: {}, 充值类型: {}, 充值金额: {}",
                message.getOrderSn(), message.getBalanceType(), message.getOrderAmount());

        try {
            // 参数校验
            validateMessage(message);

            // 根据余额类型执行不同的充值逻辑
            if (BalanceTypeEnum.ACCOUNT.equals(message.getBalanceType())) {
                handleAccountTopUp(message);
            } else if (BalanceTypeEnum.ELECTRIC_METER.equals(message.getBalanceType())) {
                handleElectricMeterTopUp(message);
            } else {
                throw new BusinessRuntimeException("不支持的充值类型: " + message.getBalanceType());
            }

            // 标记事务消息为成功
            transactionMessageService.success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, message.getOrderSn());

            log.info("能源充值处理完成，订单号: {}", message.getOrderSn());

        } catch (Exception e) {
            try {
                // 标记事务消息为失败
                transactionMessageService.failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, message.getOrderSn());
            } catch (Exception ex) {
                log.error("标记事务消息失败时发生异常，订单号: {}, 错误信息: {}", message.getOrderSn(), ex.getMessage(), ex);
            }

            log.error("处理能源充值消息失败，订单号: {}, 错误信息: {}", message.getOrderSn(), e.getMessage(), e);
        }
    }

    /**
     * 参数校验
     *
     * @param message 终止成功消息
     */
    private void validateMessage(EnergyTopUpSuccessMessage message) {
        if (BalanceTypeEnum.ELECTRIC_METER.equals(message.getBalanceType()) && message.getMeterId() == null) {
            throw new BusinessRuntimeException("电表充值时meterId不能为空");
        }
    }


    /**
     * 处理账户充值
     *
     * @param message 充值成功消息
     */
    private void handleAccountTopUp(EnergyTopUpSuccessMessage message) {
        log.info("开始处理账户充值，订单号: {}, 账户ID: {}, 充值金额: {}",
                message.getOrderSn(), message.getAccountId(), message.getOrderAmount());

        BalanceDto topUpDto = new BalanceDto()
                .setBalanceRelationId(message.getAccountId())
                .setBalanceType(message.getBalanceType())
                .setAccountId(message.getAccountId())
                .setOrderNo(message.getOrderSn())
                .setAmount(message.getOrderAmount());

        balanceService.topUp(topUpDto);
        log.info("账户充值完成，订单号: {}, 账户ID: {}", message.getOrderSn(), message.getAccountId());
    }

    /**
     * 处理电表充值
     * 包括断闸检查和开闸操作
     *
     * @param message 充值成功消息
     */
    private void handleElectricMeterTopUp(EnergyTopUpSuccessMessage message) {
        log.info("开始处理电表充值，订单号: {}, 电表ID: {}, 充值金额: {}",
                message.getOrderSn(), message.getMeterId(), message.getOrderAmount());

        // 执行充值操作
        BalanceDto topUpDto = new BalanceDto()
                .setBalanceRelationId(message.getMeterId())
                .setBalanceType(message.getBalanceType())
                .setAccountId(message.getAccountId())
                .setOrderNo(message.getOrderSn())
                .setAmount(message.getOrderAmount());

        balanceService.topUp(topUpDto);
        log.info("电表充值完成，订单号: {}, 电表ID: {}", message.getOrderSn(), message.getMeterId());

        // 无须查询电表状态，接口内部已处理
        electricMeterManagerService.setSwitchStatus(new ElectricMeterSwitchStatusDto()
                .setId(message.getMeterId())
                .setSwitchStatus(ElectricSwitchStatusEnum.ON)
                .setCommandSource(CommandSourceEnum.SYSTEM)
        );

    }
}
