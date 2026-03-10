package info.zhihui.ems.mq.rabbitmq.listener.finance;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountBalanceChangeService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.dto.MeterBalanceChangeDto;
import info.zhihui.ems.business.device.service.MeterBalanceChangeService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
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
    private final AccountInfoService accountInfoService;
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
            handleMeterBalanceChange(message);
            return;
        }
        log.warn("未支持的余额类型，message={}", message);
    }

    private void handleMeterBalanceChange(BalanceChangedMessage message) {
        try {
            meterBalanceChangeService.handleBalanceChange(new MeterBalanceChangeDto()
                    .setMeterId(message.getBalanceRelationId())
                    .setNewBalance(message.getNewBalance())
                    .setNeedHandleSwitchStatus(needHandleSwitchStatus(message.getAccountId())));
        } catch (Exception exception) {
            log.error("处理电表余额变化失败，message={}", message, exception);
        }
    }

    private boolean needHandleSwitchStatus(Integer accountId) {
        AccountBo accountBo = accountInfoService.getById(accountId);
        return ElectricAccountTypeEnum.QUANTITY == accountBo.getElectricAccountType();
    }
}
