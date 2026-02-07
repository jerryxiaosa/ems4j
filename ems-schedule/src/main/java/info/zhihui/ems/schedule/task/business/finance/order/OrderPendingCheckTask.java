package info.zhihui.ems.schedule.task.business.finance.order;

import info.zhihui.ems.business.finance.service.order.core.OrderCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单待支付状态校验计划任务
 * TryCompleteListener 消费失败时由该任务执行业务补偿，不依赖消息队列消费重试。
 *
 * @author jerryxiaosa
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPendingCheckTask {

    private final OrderCheckService orderCheckService;

    /**
     * 每小时的 15 分与 45 分执行
     */
    @Scheduled(cron = "0 15,45 * * * ?")
    public void completePendingOrdersInLast7Days() {
        orderCheckService.completePendingOrdersInLast7Days();
    }
}
