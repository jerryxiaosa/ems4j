package info.zhihui.ems.business.finance.service.order.core.impl;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.service.order.core.OrderCheckService;
import info.zhihui.ems.business.finance.service.order.core.OrderQueryService;
import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单校验服务接口
 *
 * @author jerryxiaosa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCheckServiceImpl implements OrderCheckService {

    private final OrderQueryService orderQueryService;
    private final OrderService orderService;

    /**
     * 查找过去7天内状态为待支付（OrderStatusEnum.NOT_PAY）的订单，
     * 并逐一调用 OrderService.complete(orderSn) 尝试完成订单
     */
    @Override
    public void completePendingOrdersInLast7Days() {
        log.info("开始处理过去7天内待支付订单");

        // 构造查询条件：过去7天内的待支付订单
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setCreateStartTime(sevenDaysAgo)
                .setCreateEndTime(now);

        // 查询待支付订单列表
        List<OrderBo> pendingOrders = orderQueryService.findOrders(queryDto);

        if (CollectionUtils.isEmpty(pendingOrders)) {
            log.info("未找到过去7天内的待支付订单");
            return;
        }

        log.info("找到{}条过去7天内的待支付订单，开始逐一处理", pendingOrders.size());

        int successCount = 0;
        int failureCount = 0;

        // 逐一处理每个订单
        for (OrderBo order : pendingOrders) {
            try {
                log.info("开始处理订单：{}", order.getOrderSn());
                if (!StringUtils.hasLength(order.getOrderSn())) {
                    throw new BusinessRuntimeException("数据异常，订单编号不能为空");
                }

                orderService.complete(order.getOrderSn());
                successCount++;
                log.info("订单{}处理成功", order.getOrderSn());
            } catch (Exception e) {
                failureCount++;
                log.error("订单{}处理失败，错误信息：{}", order.getOrderSn(), e.getMessage(), e);
                // 单个订单失败不影响其他订单处理，继续处理下一个订单
            }
        }

        log.info("过去7天内待支付订单处理完成，总计{}条，成功{}条，失败{}条",
                pendingOrders.size(), successCount, failureCount);

    }
}