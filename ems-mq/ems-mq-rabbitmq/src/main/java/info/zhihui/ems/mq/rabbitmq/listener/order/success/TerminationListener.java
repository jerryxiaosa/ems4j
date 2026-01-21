package info.zhihui.ems.mq.rabbitmq.listener.order.success;

import info.zhihui.ems.business.finance.dto.BalanceDeleteDto;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.message.order.status.TerminationSuccessMessage;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 订单终止成功消息监听器
 * 处理订单终止成功后的账户余额删除操作
 *
 * @author jerryxiaosa
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Validated
public class TerminationListener {

    private final BalanceService balanceService;
    private final TransactionMessageService transactionMessageService;

    @RabbitListener(queues = QueueConstant.QUEUE_ORDER_SUCCESS_TERMINATION)
    public void handleTerminationSuccess(@Valid @NotNull TerminationSuccessMessage message) {
        log.info("开始处理订单终止成功消息，订单号：{}, 账户ID：{}, 计费类型：{}, 全量销户：{}, 电表数量：{}",
                 message.getOrderSn(), message.getAccountId(), message.getElectricAccountType(),
                 message.getFullCancel(), message.getElectricMeterAmount());

        try {
            // 参数校验
            validateMessage(message);

            // 根据计费类型执行不同的销户逻辑
            processTerminationByAccountType(message);

            // 标记事务消息成功
            transactionMessageService.success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, message.getOrderSn());

            log.info("订单终止成功消息处理完成，订单号：{}", message.getOrderSn());

        } catch (Exception e) {
            log.error("处理订单终止成功消息失败，订单号：{}, 错误信息：{}", message.getOrderSn(), e.getMessage(), e);

            transactionMessageService.failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, message.getOrderSn());
        }
    }

    /**
     * 参数校验
     *
     * @param message 终止成功消息
     */
    private void validateMessage(TerminationSuccessMessage message) {
        // 校验电表数量一致性
        if (message.getElectricMeterAmount() == null ||
            !message.getElectricMeterAmount().equals(message.getMeterIdList().size())) {
            throw new BusinessRuntimeException(String.format("电表数量不一致，期望：%d，实际：%d",
                message.getElectricMeterAmount(), message.getMeterIdList().size()));
        }
    }

    /**
     * 根据计费类型处理销户逻辑
     *
     * @param message 终止成功消息
     */
    private void processTerminationByAccountType(TerminationSuccessMessage message) {
        ElectricAccountTypeEnum accountType = message.getElectricAccountType();

        if (ElectricAccountTypeEnum.QUANTITY.equals(accountType)) {
            // 按需计费：每个电表都需要销户
            processQuantityAccountTermination(message);
        } else if (ElectricAccountTypeEnum.MONTHLY.equals(accountType) ||
                   ElectricAccountTypeEnum.MERGED.equals(accountType)) {
            // 包月或合并计费：根据fullCancel标志决定是否销户
            processMonthlyOrMergedAccountTermination(message);
        } else {
            throw new BusinessRuntimeException("不支持的计费类型：" + accountType);
        }
    }

    /**
     * 处理按需计费账户销户
     * 每个电表都需要单独销户
     *
     * @param message 终止成功消息
     */
    private void processQuantityAccountTermination(TerminationSuccessMessage message) {
        log.info("开始处理按需计费账户销户，账户ID：{}, 电表数量：{}",
                message.getAccountId(), message.getMeterIdList().size());

        List<Integer> meterIdList = message.getMeterIdList();
        int successCount = 0;
        for (Integer meterId : meterIdList) {
            try {
                BalanceDeleteDto deleteDto = new BalanceDeleteDto()
                    .setBalanceRelationId(meterId)
                    .setBalanceType(BalanceTypeEnum.ELECTRIC_METER);

                balanceService.deleteBalance(deleteDto);

                log.info("电表余额删除成功，电表ID：{}, 订单号：{}", meterId, message.getOrderSn());
                successCount ++;
            } catch (Exception e) {
                log.error("电表余额删除失败，电表ID：{}, 订单号：{}, 错误：{}",
                        meterId, message.getOrderSn(), e.getMessage());
            }
        }

        if (successCount != meterIdList.size()) {
            throw new BusinessRuntimeException("部分电表余额删除失败");
        }

        log.info("按需计费账户销户完成，账户ID：{}, 成功销户电表数量：{}", message.getAccountId(), meterIdList.size());
    }

    /**
     * 处理包月或合并计费账户销户
     * 只有当fullCancel为true时才进行整个账户销户
     *
     * @param message 终止成功消息
     */
    private void processMonthlyOrMergedAccountTermination(TerminationSuccessMessage message) {
        log.info("开始处理{}账户销户，账户ID：{}, 全量销户：{}",
                message.getElectricAccountType().getInfo(), message.getAccountId(), message.getFullCancel());

        if (Boolean.TRUE.equals(message.getFullCancel())) {
            try {
                BalanceDeleteDto deleteDto = new BalanceDeleteDto()
                    .setBalanceRelationId(message.getAccountId())
                    .setBalanceType(BalanceTypeEnum.ACCOUNT);

                balanceService.deleteBalance(deleteDto);

                log.info("账户余额删除成功，账户ID：{}, 订单号：{}",
                        message.getAccountId(), message.getOrderSn());

            } catch (Exception e) {
                log.error("账户余额删除失败，账户ID：{}, 订单号：{}, 错误：{}",
                        message.getAccountId(), message.getOrderSn(), e.getMessage());
                throw new BusinessRuntimeException("账户余额删除失败，账户ID：" + message.getAccountId());
            }
        } else {
            log.info("{}账户未设置全量销户，跳过余额删除，账户ID：{}",
                    message.getElectricAccountType().getInfo(), message.getAccountId());
        }
    }
}