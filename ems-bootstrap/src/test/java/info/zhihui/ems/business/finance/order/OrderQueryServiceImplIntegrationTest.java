package info.zhihui.ems.business.finance.order;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.OrderQueryService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
 * OrderQueryServiceImpl 集成测试
 *
 * @author jerryxiaosa
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class OrderQueryServiceImplIntegrationTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderRepository.delete(null);
    }

    @Test
    void findOrdersPage_ShouldReturnOrderList_WhenOrdersExist() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysAgo = now.minusDays(3);
        LocalDateTime fiveDaysAgo = now.minusDays(5);

        // 创建不同状态的订单
        OrderEntity notPayOrder1 = buildOrderEntity("IT-QUERY-001", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity notPayOrder2 = buildOrderEntity("IT-QUERY-002", fiveDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity successOrder = buildOrderEntity("IT-QUERY-003", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());

        orderRepository.insert(notPayOrder1);
        orderRepository.insert(notPayOrder2);
        orderRepository.insert(successOrder);

        // 构造查询条件 - 查询待支付订单
        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setCreateStartTime(now.minusDays(7))
                .setCreateEndTime(now);

        // 执行查询
        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderBo> result = pageResult.getList();

        // 验证结果
        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderBo::getOrderSn)
                .containsExactlyInAnyOrder("IT-QUERY-001", "IT-QUERY-002");
        assertThat(result).allMatch(order -> order.getOrderStatus() == OrderStatusEnum.NOT_PAY);
    }

    @Test
    void findOrdersPage_ShouldReturnEmptyList_WhenNoOrdersMatch() {
        // 准备测试数据 - 创建不符合条件的订单
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenDaysAgo = now.minusDays(10);

        OrderEntity oldOrder = buildOrderEntity("IT-QUERY-OLD", tenDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(oldOrder);

        // 构造查询条件 - 查询过去7天内的订单
        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setCreateStartTime(now.minusDays(7))
                .setCreateEndTime(now);

        // 执行查询
        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderBo> result = pageResult.getList();

        // 验证结果
        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void findOrdersPage_ShouldFilterByStatus_WhenStatusProvided() {
        // 准备测试数据
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity notPayOrder = buildOrderEntity("IT-QUERY-NOT-PAY", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity successOrder = buildOrderEntity("IT-QUERY-SUCCESS", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());
        OrderEntity closedOrder = buildOrderEntity("IT-QUERY-CLOSED", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.CLOSED.name());

        orderRepository.insert(notPayOrder);
        orderRepository.insert(successOrder);
        orderRepository.insert(closedOrder);

        // 构造查询条件 - 只查询成功订单
        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.SUCCESS);

        // 执行查询
        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderBo> result = pageResult.getList();

        // 验证结果
        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-SUCCESS");
        assertThat(result.get(0).getOrderStatus()).isEqualTo(OrderStatusEnum.SUCCESS);
    }

    @Test
    void findOrdersPage_ShouldFilterByPaymentChannel_WhenPaymentChannelProvided() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity wxOrder = buildOrderEntity("IT-QUERY-PAY-WX", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name());
        OrderEntity offlineOrder = buildOrderEntity("IT-QUERY-PAY-OFFLINE", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name());

        orderRepository.insert(wxOrder);
        orderRepository.insert(offlineOrder);

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderBo> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-PAY-OFFLINE");
        assertThat(result.get(0).getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
    }

    @Test
    void findOrdersPage_ShouldFilterByUserId_WhenUserIdProvided() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity user1001Order = buildOrderEntity("IT-QUERY-USER-1001", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setUserId(1001);
        OrderEntity user2002Order = buildOrderEntity("IT-QUERY-USER-2002", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setUserId(2002);

        orderRepository.insert(user1001Order);
        orderRepository.insert(user2002Order);

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setUserId(2002);

        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderBo> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-USER-2002");
        assertThat(result.get(0).getUserId()).isEqualTo(2002);
    }

    @Test
    void findOrdersPage_ShouldFilterByTimeRange_WhenTimeRangeProvided() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysAgo = now.minusDays(2);
        LocalDateTime fiveDaysAgo = now.minusDays(5);
        LocalDateTime tenDaysAgo = now.minusDays(10);

        OrderEntity recentOrder = buildOrderEntity("IT-QUERY-RECENT", twoDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity mediumOrder = buildOrderEntity("IT-QUERY-MEDIUM", fiveDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity oldOrder = buildOrderEntity("IT-QUERY-OLD", tenDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(recentOrder);
        orderRepository.insert(mediumOrder);
        orderRepository.insert(oldOrder);

        // 构造查询条件 - 查询过去7天内的订单
        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setCreateStartTime(now.minusDays(7))
                .setCreateEndTime(now);

        // 执行查询
        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderBo> result = pageResult.getList();

        // 验证结果
        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderBo::getOrderSn)
                .containsExactlyInAnyOrder("IT-QUERY-RECENT", "IT-QUERY-MEDIUM");
    }

    @Test
    void findOrdersPage_ShouldReturnOrderedByCreateTime_WhenMultipleOrdersExist() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);
        LocalDateTime threeDaysAgo = now.minusDays(3);

        OrderEntity order1 = buildOrderEntity("IT-QUERY-ORDER-1", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity order2 = buildOrderEntity("IT-QUERY-ORDER-2", oneDayAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity order3 = buildOrderEntity("IT-QUERY-ORDER-3", twoDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(order1);
        orderRepository.insert(order2);
        orderRepository.insert(order3);

        // 构造查询条件
        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        // 执行查询
        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderBo> result = pageResult.getList();

        // 验证结果 - 应该按创建时间降序排列
        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-ORDER-2"); // 最新的
        assertThat(result.get(1).getOrderSn()).isEqualTo("IT-QUERY-ORDER-3"); // 中间的
        assertThat(result.get(2).getOrderSn()).isEqualTo("IT-QUERY-ORDER-1"); // 最早的
    }

    @Test
    void findOrdersPage_ShouldExcludeDeletedOrders_WhenDeletedOrdersExist() {
        // 准备测试数据
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity normalOrder = buildOrderEntity("IT-QUERY-NORMAL", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setIsDeleted(Boolean.FALSE);
        OrderEntity deletedOrder = buildOrderEntity("IT-QUERY-DELETED", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setIsDeleted(Boolean.TRUE);

        orderRepository.insert(normalOrder);
        orderRepository.insert(deletedOrder);

        // 构造查询条件
        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        // 执行查询
        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderBo> result = pageResult.getList();

        // 验证结果 - 应该只返回未删除的订单
        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-NORMAL");
    }

    @Test
    void findOrdersPage_ShouldReturnPageMeta_WhenPaginationProvided() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        OrderEntity newestOrder = buildOrderEntity("IT-PAGE-META-NEW", now.minusHours(1))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity middleOrder = buildOrderEntity("IT-PAGE-META-MID", now.minusHours(2))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity oldestOrder = buildOrderEntity("IT-PAGE-META-OLD", now.minusHours(3))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(newestOrder);
        orderRepository.insert(middleOrder);
        orderRepository.insert(oldestOrder);

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        // 执行查询 - 每页2条，查第1页
        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(2));

        // 验证分页元数据
        assertThat(pageResult).isNotNull();
        assertThat(pageResult.getPageNum()).isEqualTo(1);
        assertThat(pageResult.getPageSize()).isEqualTo(2);
        assertThat(pageResult.getTotal()).isEqualTo(3);
        assertThat(pageResult.getList()).hasSize(2);
    }

    @Test
    void findOrdersPage_ShouldReturnSecondPageData_WhenRequestSecondPage() {
        // 准备测试数据（按创建时间降序：NEW -> MID -> OLD）
        LocalDateTime now = LocalDateTime.now();
        OrderEntity newestOrder = buildOrderEntity("IT-PAGE-NEW", now.minusHours(1))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity middleOrder = buildOrderEntity("IT-PAGE-MID", now.minusHours(2))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity oldestOrder = buildOrderEntity("IT-PAGE-OLD", now.minusHours(3))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(newestOrder);
        orderRepository.insert(middleOrder);
        orderRepository.insert(oldestOrder);

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        // 执行查询 - 每页2条，查第2页，应只剩最早一条
        PageResult<OrderBo> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(2).setPageSize(2));
        List<OrderBo> result = pageResult.getList();

        // 验证第2页数据
        assertThat(pageResult).isNotNull();
        assertThat(pageResult.getPageNum()).isEqualTo(2);
        assertThat(pageResult.getPageSize()).isEqualTo(2);
        assertThat(pageResult.getTotal()).isEqualTo(3);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-PAGE-OLD");
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
