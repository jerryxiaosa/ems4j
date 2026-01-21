package info.zhihui.ems.business.finance.order;

import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.OrderCheckService;
import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * OrderCheckServiceImpl 集成测试
 *
 * @author jerryxiaosa
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class OrderCheckServiceImplIntegrationTest {

    @Autowired
    private OrderCheckService orderCheckService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderRepository.delete(null);
    }

    @Test
    void completePendingOrdersInLast7Days_ShouldCompleteValidOrders_WhenOrdersExist() {
        // 准备测试数据 - 创建过去7天内的待支付订单
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysAgo = now.minusDays(3);
        LocalDateTime fiveDaysAgo = now.minusDays(5);

        // 创建有效的待支付订单
        OrderEntity validOrder1 = buildOrderEntity("IT-CHECK-001", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity validOrder2 = buildOrderEntity("IT-CHECK-002", fiveDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(validOrder1);
        orderRepository.insert(validOrder2);

        // Mock OrderService.complete 方法成功执行
        doNothing().when(orderService).complete(anyString());

        // 执行测试方法
        orderCheckService.completePendingOrdersInLast7Days();

        // 验证OrderService.complete被调用了正确的次数
        verify(orderService, times(2)).complete(anyString());
        verify(orderService).complete("IT-CHECK-001");
        verify(orderService).complete("IT-CHECK-002");
    }

    @Test
    void completePendingOrdersInLast7Days_ShouldSkipInvalidOrders_WhenOrdersHaveIssues() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysAgo = now.minusDays(3);
        LocalDateTime tenDaysAgo = now.minusDays(10);

        // 创建各种状态的订单
        OrderEntity validOrder = buildOrderEntity("IT-CHECK-VALID", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        OrderEntity completedOrder = buildOrderEntity("IT-CHECK-COMPLETED", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());

        OrderEntity oldOrder = buildOrderEntity("IT-CHECK-OLD", tenDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        OrderEntity emptyOrderSnOrder = buildOrderEntity("", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(validOrder);
        orderRepository.insert(completedOrder);
        orderRepository.insert(oldOrder);
        orderRepository.insert(emptyOrderSnOrder);

        // Mock OrderService.complete 方法
        doNothing().when(orderService).complete("IT-CHECK-VALID");

        // 执行测试方法
        orderCheckService.completePendingOrdersInLast7Days();

        // 验证只有有效订单被处理
        verify(orderService, times(1)).complete(anyString());
        verify(orderService).complete("IT-CHECK-VALID");
        verify(orderService, never()).complete("IT-CHECK-COMPLETED");
        verify(orderService, never()).complete("IT-CHECK-OLD");
    }

    @Test
    void completePendingOrdersInLast7Days_ShouldHandleExceptions_WhenOrderServiceFails() {
        // 准备测试数据
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity order1 = buildOrderEntity("IT-CHECK-FAIL-001", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity order2 = buildOrderEntity("IT-CHECK-SUCCESS-002", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(order1);
        orderRepository.insert(order2);

        // Mock OrderService.complete 方法 - 第一个订单失败，第二个成功
        doThrow(new BusinessRuntimeException("订单处理失败"))
                .when(orderService).complete("IT-CHECK-FAIL-001");
        doNothing().when(orderService).complete("IT-CHECK-SUCCESS-002");

        // 执行测试方法 - 应该不抛出异常
        orderCheckService.completePendingOrdersInLast7Days();

        // 验证两个订单都被尝试处理
        verify(orderService).complete("IT-CHECK-FAIL-001");
        verify(orderService).complete("IT-CHECK-SUCCESS-002");
    }

    @Test
    void completePendingOrdersInLast7Days_ShouldDoNothing_WhenNoOrdersFound() {
        // 不插入任何订单数据

        // 执行测试方法
        orderCheckService.completePendingOrdersInLast7Days();

        // 验证OrderService.complete没有被调用
        verify(orderService, never()).complete(anyString());
    }

    @Test
    void completePendingOrdersInLast7Days_ShouldOnlyProcessNotPayOrders_WhenMixedStatusExists() {
        // 准备测试数据
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity notPayOrder = buildOrderEntity("IT-CHECK-NOT-PAY", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity successOrder = buildOrderEntity("IT-CHECK-SUCCESS", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());
        OrderEntity closedOrder = buildOrderEntity("IT-CHECK-CLOSED", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.CLOSED.name());

        orderRepository.insert(notPayOrder);
        orderRepository.insert(successOrder);
        orderRepository.insert(closedOrder);

        // Mock OrderService.complete 方法
        doNothing().when(orderService).complete("IT-CHECK-NOT-PAY");

        // 执行测试方法
        orderCheckService.completePendingOrdersInLast7Days();

        // 验证只有NOT_PAY状态的订单被处理
        verify(orderService, times(1)).complete(anyString());
        verify(orderService).complete("IT-CHECK-NOT-PAY");
        verify(orderService, never()).complete("IT-CHECK-SUCCESS");
        verify(orderService, never()).complete("IT-CHECK-CLOSED");
    }

    /**
     * 构建测试用的订单实体
     */
    private OrderEntity buildOrderEntity(String orderSn, LocalDateTime createTime) {
        return new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1001)
                .setUserRealName("集成测试用户")
                .setUserPhone("13800000001")
                .setThirdPartyUserId("test_user_id")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setServiceRate(BigDecimal.ZERO)
                .setServiceAmount(BigDecimal.ZERO)
                .setUserPayAmount(new BigDecimal("100.00"))
                .setCurrency("CNY")
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(createTime)
                .setIsDeleted(Boolean.FALSE);
    }
}