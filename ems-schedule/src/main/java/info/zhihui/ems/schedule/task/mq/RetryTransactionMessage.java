package info.zhihui.ems.schedule.task.mq;

import info.zhihui.ems.mq.api.bo.TransactionMessageBo;
import info.zhihui.ems.mq.api.service.MqService;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 事务消息重试计划任务
 * 每分钟执行一次，获取失败的事务消息并重新投递到消息队列
 * 消息执行结果由消息队列处理后自动标记，无需手动更新状态
 * 用于补偿 EnergyTopUpListener、TerminationListener 等事务消息链路的消费失败。
 *
 * @author jerryxiaosa
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryTransactionMessage {

    private final TransactionMessageService transactionMessageService;
    private final MqService mqService;

    /**
     * 每分钟的0秒执行事务消息重试任务
     */
    @Scheduled(cron = "0 * * * * ?")
    public void retryFailedTransactionMessages() {
        log.info("开始执行事务消息重试任务");

        // 获取失败的事务消息记录
        List<TransactionMessageBo> failedMessages = transactionMessageService.findRecentFailureRecords();

        if (CollectionUtils.isEmpty(failedMessages)) {
            log.info("未找到需要重试的事务消息");
            return;
        }

        log.info("找到{}条需要重试的事务消息", failedMessages.size());

        // 遍历处理每条失败记录
        for (TransactionMessageBo messageBo : failedMessages) {
            try {
                // 重新投递消息到队列，执行结果由消息队列处理后自动标记
                mqService.sendMessage(messageBo.getMessage());

                log.debug("事务消息投递成功: businessType={}, sn={}",
                        messageBo.getBusinessType(), messageBo.getSn());

            } catch (Exception e) {
                log.error("事务消息投递失败: businessType={}, sn={}, error={}",
                        messageBo.getBusinessType(), messageBo.getSn(), e.getMessage(), e);
                try {
                    transactionMessageService.failure(messageBo.getBusinessType(), messageBo.getSn());
                } catch (Exception ex) {
                    log.error("更新事务消息失败状态失败: businessType={}, sn={}, error={}",
                            messageBo.getBusinessType(), messageBo.getSn(), ex.getMessage(), ex);
                }
            }
        }

        log.info("事务消息重试任务完成");

    }
}
