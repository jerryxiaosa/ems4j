package info.zhihui.ems.business.order;

import info.zhihui.ems.business.order.dto.OrderQueryDto;
import info.zhihui.ems.business.order.dto.OrderListDto;
import info.zhihui.ems.business.billing.entity.OrderFlowEntity;
import info.zhihui.ems.business.order.entity.OrderDetailEnergyTopUpEntity;
import info.zhihui.ems.business.order.entity.OrderEntity;
import info.zhihui.ems.business.order.entity.OrderThirdPartyPrepayEntity;
import info.zhihui.ems.business.order.enums.OrderStatusEnum;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.order.enums.PaymentChannelEnum;
import info.zhihui.ems.business.billing.repository.OrderFlowRepository;
import info.zhihui.ems.business.order.repository.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.order.repository.OrderRepository;
import info.zhihui.ems.business.order.repository.OrderThirdPartyPrepayRepository;
import info.zhihui.ems.business.order.service.core.OrderQueryService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
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

    @Autowired
    private OrderThirdPartyPrepayRepository orderThirdPartyPrepayRepository;

    @Autowired
    private OrderFlowRepository orderFlowRepository;

    @Autowired
    private OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderRepository.delete(null);
        orderThirdPartyPrepayRepository.delete(null);
        orderFlowRepository.delete(null);
        orderDetailEnergyTopUpRepository.delete(null);
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
        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        // 验证结果
        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderListDto::getOrderSn)
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
        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

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
        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

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

        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-PAY-OFFLINE");
        assertThat(result.get(0).getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
    }

    @Test
    void findOrdersPage_ShouldFilterByOrderType_WhenOrderTypeProvided() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity energyTopUpOrder = buildOrderEntity("IT-QUERY-TYPE-ENERGY", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode());
        OrderEntity terminationOrder = buildOrderEntity("IT-QUERY-TYPE-TERM", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setOrderType(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT.getCode());

        orderRepository.insert(energyTopUpOrder);
        orderRepository.insert(terminationOrder);

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setOrderType(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT);

        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-TYPE-TERM");
        assertThat(result.get(0).getOrderType()).isEqualTo(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT);
    }

    @Test
    void findOrdersPage_ShouldFilterByOrderSnLike_WhenOrderSnLikeProvided() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity matchedOrder = buildOrderEntity("IT-QUERY-LIKE-ABC-001", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity unmatchedOrder = buildOrderEntity("IT-QUERY-LIKE-XYZ-002", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(matchedOrder);
        orderRepository.insert(unmatchedOrder);

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setOrderSnLike("ABC");

        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-LIKE-ABC-001");
    }

    @Test
    void findOrdersPage_ShouldFilterByThirdPartySnLike_WhenThirdPartySnLikeProvided() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity matchedOrder = buildOrderEntity("IT-QUERY-THIRD-SN-01", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        OrderEntity unmatchedOrder = buildOrderEntity("IT-QUERY-THIRD-SN-02", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name());

        orderRepository.insert(matchedOrder);
        orderRepository.insert(unmatchedOrder);

        orderThirdPartyPrepayRepository.insert(new OrderThirdPartyPrepayEntity()
                .setOrderSn("IT-QUERY-THIRD-SN-01")
                .setPrepayId("prepay-matched")
                .setThirdPartyUserId("tp-user-1")
                .setThirdPartySn("wx-abc-001")
                .setPrepayAt(LocalDateTime.now())
                .setIsDeleted(Boolean.FALSE));
        orderThirdPartyPrepayRepository.insert(new OrderThirdPartyPrepayEntity()
                .setOrderSn("IT-QUERY-THIRD-SN-02")
                .setPrepayId("prepay-unmatched")
                .setThirdPartyUserId("tp-user-2")
                .setThirdPartySn("wx-xyz-002")
                .setPrepayAt(LocalDateTime.now())
                .setIsDeleted(Boolean.FALSE));

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setThirdPartySnLike("abc");

        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-THIRD-SN-01");
        assertThat(result.get(0).getThirdPartySn()).isEqualTo("wx-abc-001");
    }

    @Test
    void findOrdersPage_ShouldReturnProcessBalanceFields_WhenOrderFlowExists() {
        LocalDateTime now = LocalDateTime.now();

        OrderEntity orderEntity = buildOrderEntity("IT-QUERY-BALANCE-FLOW", now.minusHours(1))
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());
        orderRepository.insert(orderEntity);

        orderFlowRepository.insert(new OrderFlowEntity()
                .setConsumeId("IT-QUERY-BALANCE-FLOW")
                .setBalanceRelationId(1)
                .setBalanceType(0)
                .setAccountId(1)
                .setAmount(new BigDecimal("100.00"))
                .setBeginBalance(new BigDecimal("500.00"))
                .setEndBalance(new BigDecimal("600.00"))
                .setCreateTime(now));

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.SUCCESS);

        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderSn()).isEqualTo("IT-QUERY-BALANCE-FLOW");
        assertThat(result.get(0).getBeginBalance()).isEqualByComparingTo("500.00");
        assertThat(result.get(0).getEndBalance()).isEqualByComparingTo("600.00");
    }

    @Test
    void getOrderDetail_ShouldReturnProcessBalanceFields_WhenOrderFlowExists() {
        LocalDateTime now = LocalDateTime.now();

        OrderEntity orderEntity = buildOrderEntity("IT-DETAIL-BAL-FLOW", now.minusHours(1))
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());
        orderRepository.insert(orderEntity);
        orderDetailEnergyTopUpRepository.insert(new OrderDetailEnergyTopUpEntity()
                .setOrderSn("IT-DETAIL-BAL-FLOW")
                .setAccountId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setMeterId(1)
                .setTopUpAmount(new BigDecimal("95.00"))
                .setCreateTime(now));

        orderFlowRepository.insert(new OrderFlowEntity()
                .setConsumeId("IT-DETAIL-BAL-FLOW")
                .setBalanceRelationId(1)
                .setBalanceType(0)
                .setAccountId(1)
                .setAmount(new BigDecimal("100.00"))
                .setBeginBalance(new BigDecimal("500.00"))
                .setEndBalance(new BigDecimal("600.00"))
                .setCreateTime(now));

        var result = orderQueryService.getOrderDetail("IT-DETAIL-BAL-FLOW");

        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isEqualTo("IT-DETAIL-BAL-FLOW");
        assertThat(result.getBeginBalance()).isEqualByComparingTo("500.00");
        assertThat(result.getEndBalance()).isEqualByComparingTo("600.00");
        assertThat(result.getTopUpAmount()).isEqualByComparingTo("95.00");
    }

    @Test
    void findOrdersPage_ShouldReturnMeterInfoOnlyForElectricMeterTopUp() {
        LocalDateTime now = LocalDateTime.now();

        OrderEntity electricMeterOrder = buildOrderEntity("IT-QUERY-METER-ELE", now.minusMinutes(2))
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());
        OrderEntity accountOrder = buildOrderEntity("IT-QUERY-METER-ACC", now.minusMinutes(1))
                .setOrderStatus(OrderStatusEnum.SUCCESS.name());
        orderRepository.insert(electricMeterOrder);
        orderRepository.insert(accountOrder);

        orderDetailEnergyTopUpRepository.insert(new OrderDetailEnergyTopUpEntity()
                .setOrderSn("IT-QUERY-METER-ELE")
                .setAccountId(1001)
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setMeterId(2001)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setMeterName("A区1号电表")
                .setDeviceNo("DNO-001")
                .setTopUpAmount(new BigDecimal("100.00"))
                .setCreateTime(now));
        orderDetailEnergyTopUpRepository.insert(new OrderDetailEnergyTopUpEntity()
                .setOrderSn("IT-QUERY-METER-ACC")
                .setAccountId(1001)
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setMeterId(2002)
                .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode())
                .setMeterName("不应返回")
                .setDeviceNo("DNO-999")
                .setTopUpAmount(new BigDecimal("88.00"))
                .setCreateTime(now));

        OrderQueryDto queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.SUCCESS)
                .setOrderSnLike("IT-QUERY-METER");

        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).hasSize(2);
        OrderListDto electricMeterOrderResult = result.stream()
                .filter(item -> "IT-QUERY-METER-ELE".equals(item.getOrderSn()))
                .findFirst()
                .orElseThrow();
        OrderListDto accountOrderResult = result.stream()
                .filter(item -> "IT-QUERY-METER-ACC".equals(item.getOrderSn()))
                .findFirst()
                .orElseThrow();

        assertThat(electricMeterOrderResult.getMeterName()).isEqualTo("A区1号电表");
        assertThat(electricMeterOrderResult.getDeviceNo()).isEqualTo("DNO-001");
        assertThat(accountOrderResult.getMeterName()).isNull();
        assertThat(accountOrderResult.getDeviceNo()).isNull();
    }

    @Test
    void findOrdersPage_ShouldFilterByEnterpriseNameLike_AndExcludePersonalOrders() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        OrderEntity enterpriseMatchedOrder = buildOrderEntity("IT-QUERY-ENT-MATCH", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerName("华星科技有限公司");
        OrderEntity enterpriseUnmatchedOrder = buildOrderEntity("IT-QUERY-ENT-UNMATCH", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerName("晨光物流有限公司");
        OrderEntity personalSameNameOrder = buildOrderEntity("IT-QUERY-PERSONAL-SAME", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setOwnerType(OwnerTypeEnum.PERSONAL.getCode())
                .setOwnerName("华星科技有限公司");
        OrderEntity personalSimilarNameOrder = buildOrderEntity("IT-QUERY-PERSONAL-SIMILAR", threeDaysAgo)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setOwnerType(OwnerTypeEnum.PERSONAL.getCode())
                .setOwnerName("华星科技");

        orderRepository.insert(enterpriseMatchedOrder);
        orderRepository.insert(enterpriseUnmatchedOrder);
        orderRepository.insert(personalSameNameOrder);
        orderRepository.insert(personalSimilarNameOrder);

        OrderQueryDto queryDto = new OrderQueryDto()
                .setEnterpriseNameLike("华星科技");

        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        assertThat(pageResult).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).extracting(OrderListDto::getOrderSn)
                .containsExactly("IT-QUERY-ENT-MATCH");
        assertThat(result).allMatch(order -> order.getOwnerType() == OwnerTypeEnum.ENTERPRISE);
        assertThat(result).extracting(OrderListDto::getOrderSn)
                .doesNotContain("IT-QUERY-PERSONAL-SAME", "IT-QUERY-PERSONAL-SIMILAR");
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
        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

        // 验证结果
        assertThat(pageResult).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderListDto::getOrderSn)
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
        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

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
        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(10));
        List<OrderListDto> result = pageResult.getList();

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
        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(1).setPageSize(2));

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
        PageResult<OrderListDto> pageResult = orderQueryService.findOrdersPage(queryDto, new PageParam().setPageNum(2).setPageSize(2));
        List<OrderListDto> result = pageResult.getList();

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
