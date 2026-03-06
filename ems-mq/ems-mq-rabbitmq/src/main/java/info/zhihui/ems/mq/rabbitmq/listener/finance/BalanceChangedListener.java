package info.zhihui.ems.mq.rabbitmq.listener.finance;

import info.zhihui.ems.business.account.service.AccountBalanceChangeService;
import info.zhihui.ems.business.account.service.MeterBalanceChangeService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 余额变动事件监听器
 *
 * @author jerryxiaosa
 */
@Component
@Slf4j
@Validated
@RequiredArgsConstructor
public class BalanceChangedListener {

    private final AccountBalanceChangeService accountBalanceChangeService;
    private final MeterBalanceChangeService meterBalanceChangeService;

    @RabbitListener(queues = QueueConstant.QUEUE_FINANCE_BALANCE_CHANGED)
    public void handle(@Valid @NotNull BalanceChangedMessage message) {
        log.info("接收到余额变动消息，balanceType={}, relationId={}, accountId={}",
                message.getBalanceType(), message.getBalanceRelationId(), message.getAccountId());
        if (BalanceTypeEnum.ACCOUNT.equals(message.getBalanceType())) {
            accountBalanceChangeService.handleBalanceChange(message);
            return;
        }
        if (BalanceTypeEnum.ELECTRIC_METER.equals(message.getBalanceType())) {
            meterBalanceChangeService.handleBalanceChange(message);
            return;
        }
        log.warn("未支持的余额类型，message={}", message);
    }
}
