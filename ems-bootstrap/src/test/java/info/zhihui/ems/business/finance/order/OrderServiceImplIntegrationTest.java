package info.zhihui.ems.business.finance.order;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.dto.order.creation.*;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxPayConfig;
import info.zhihui.ems.business.finance.entity.order.*;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.repository.order.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.finance.repository.order.OrderDetailTerminationRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.business.finance.service.order.fee.ServiceRateService;
import info.zhihui.ems.business.finance.service.order.thirdparty.wx.sdk.WxMiniProgramPaySdk;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.system.service.ConfigService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static info.zhihui.ems.foundation.system.constant.SystemConfigConstant.WX_PAY_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class OrderServiceImplIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;

    @Autowired
    private OrderDetailTerminationRepository orderDetailTerminationRepository;

    @MockitoBean
    private WxMiniProgramPaySdk wxMiniProgramPaySdk;

    @MockitoBean
    private ConfigService configService;

    @MockitoBean
    private ServiceRateService serviceRateService;

    @MockitoBean
    private Transaction transaction;

    @Test
    void getDetail_ShouldReturnOrderBo_WhenOrderExists() {
        String orderSn = "IT-ORDER-001";
        OrderEntity entity = buildOrderEntity(orderSn)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name());
        orderRepository.insert(entity);

        OrderBo result = orderService.getDetail(orderSn);

        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isEqualTo(orderSn);
        assertThat(result.getOrderType()).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatusEnum.NOT_PAY);
        assertThat(result.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        assertThat(result.getOrderAmount()).isEqualByComparingTo("99.98");
        assertThat(result.getUserRealName()).isEqualTo("测试用户");
        assertThat(result.getUserPhone()).isEqualTo("13800000000");
        assertThat(result.getServiceRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getServiceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getDetail_ShouldReturnOrderBo_WhenEnumValuesUnexpected() {
        String orderSn = "IT-ORDER-UNKNOWN";
        OrderEntity entity = buildOrderEntity(orderSn)
                .setOrderStatus("UNKNOWN_STATUS")
                .setPaymentChannel("UNKNOWN_CHANNEL");
        orderRepository.insert(entity);

        OrderBo result = orderService.getDetail(orderSn);

        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isEqualTo(orderSn);
        assertThat(result.getOrderStatus()).isNull();
        assertThat(result.getPaymentChannel()).isNull();
        assertThat(result.getOrderType()).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
    }

    @Test
    void getDetail_ShouldThrowNotFound_WhenOrderMissing() {
        assertThatThrownBy(() -> orderService.getDetail("NOT-EXIST"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");
    }

    @Test
    void getDetail_ShouldThrowNotFound_WhenOrderDeleted() {
        String orderSn = "IT-ORDER-DELETED";
        OrderEntity entity = buildOrderEntity(orderSn)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name())
                .setIsDeleted(Boolean.TRUE);
        orderRepository.insert(entity);

        assertThatThrownBy(() -> orderService.getDetail(orderSn))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");
    }

    @Test
    void createOrder_ShouldCreateOrderSuccessfully_WhenWxMiniProgramPayment() {
        // Mock微信支付SDK调用
        when(wxMiniProgramPaySdk.getPrepayId(any(), any()))
                .thenReturn("wx_prepay_id_123456");
        when(serviceRateService.getDefaultServiceRate()).thenReturn(new BigDecimal("1.00"));

        // 创建测试订单信息
        EnergyOrderCreationInfoDto orderCreationInfo = new EnergyOrderCreationInfoDto();
        orderCreationInfo.setUserId(1001)
                .setUserPhone("13800000001")
                .setUserRealName("集成测试用户")
                .setThirdPartyUserId("wx_openid_test")
                .setOrderAmount(new BigDecimal("88.88"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI);

        // 设置energyTopUpDto属性
        EnergyTopUpDto energyTopUpDto = new EnergyTopUpDto();
        energyTopUpDto.setAccountId(1001);
        energyTopUpDto.setBalanceType(BalanceTypeEnum.ACCOUNT);
        energyTopUpDto.setOwnerType(OwnerTypeEnum.ENTERPRISE);
        energyTopUpDto.setOwnerId(2001);
        energyTopUpDto.setOwnerName("测试企业");
        energyTopUpDto.setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);
        energyTopUpDto.setMeterId(3001);
        energyTopUpDto.setMeterType(MeterTypeEnum.ELECTRIC);
        energyTopUpDto.setMeterName("测试电表001");
        energyTopUpDto.setDeviceNo("EM001");
        energyTopUpDto.setSpaceId(4001);
        orderCreationInfo.setEnergyTopUpDto(energyTopUpDto);

        // 执行订单创建
        OrderCreationResponseDto result = orderService.createOrder(orderCreationInfo);

        // 验证返回结果
        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isNotBlank();
        assertThat(result.getOrderTypeEnum()).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
        assertThat(result.getPaymentChannel()).isEqualTo(PaymentChannelEnum.WX_MINI);

        // 验证订单已保存到数据库
        OrderBo savedOrder = orderService.getDetail(result.getOrderSn());
        assertThat(savedOrder).isNotNull();

        // 详细验证savedOrder的各个字段
        assertThat(savedOrder.getOrderSn()).isEqualTo(result.getOrderSn());
        assertThat(savedOrder.getUserId()).isEqualTo(1001);
        assertThat(savedOrder.getUserPhone()).isEqualTo("13800000001");
        assertThat(savedOrder.getUserRealName()).isEqualTo("集成测试用户");
        assertThat(savedOrder.getThirdPartyUserId()).isEqualTo("wx_openid_test");
        assertThat(savedOrder.getOrderType()).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
        assertThat(savedOrder.getOrderAmount()).isEqualByComparingTo("88.88");
        assertThat(savedOrder.getServiceRate()).isEqualByComparingTo("1.00");
        assertThat(savedOrder.getUserPayAmount()).isEqualByComparingTo("177.76"); // 88.88 + 1.00
        assertThat(savedOrder.getCurrency()).isEqualTo("CNY");
        assertThat(savedOrder.getPaymentChannel()).isEqualTo(PaymentChannelEnum.WX_MINI);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.NOT_PAY);

        // 验证order_detail_energy_top_up表中的数据
        List<OrderDetailEnergyTopUpEntity> topUpDetails = orderDetailEnergyTopUpRepository.selectList(
                new QueryWrapper<OrderDetailEnergyTopUpEntity>()
                        .eq("order_sn", result.getOrderSn())
        );

        assertThat(topUpDetails).isNotEmpty();
        assertThat(topUpDetails).hasSize(1);

        OrderDetailEnergyTopUpEntity topUpDetail = topUpDetails.get(0);
        assertThat(topUpDetail.getOrderSn()).isEqualTo(result.getOrderSn());
        assertThat(topUpDetail.getAccountId()).isEqualTo(1001);
        assertThat(topUpDetail.getBalanceType()).isEqualTo(BalanceTypeEnum.ACCOUNT.getCode());
        assertThat(topUpDetail.getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE.getCode());
        assertThat(topUpDetail.getOwnerId()).isEqualTo(2001);
        assertThat(topUpDetail.getMeterId()).isEqualTo(3001);
        assertThat(topUpDetail.getMeterName()).isEqualTo("测试电表001");
        assertThat(topUpDetail.getDeviceNo()).isEqualTo("EM001");
        assertThat(topUpDetail.getSpaceId()).isEqualTo(4001);
        assertThat(topUpDetail.getCreateTime()).isNotNull();
        assertThat(topUpDetail.getCreateTime()).isEqualTo(savedOrder.getOrderCreateTime());
    }

    @Test
    void
    createOrder_ShouldCreateOrderSuccessfully_WhenTerminationOrder() {
        // Mock默认服务费率
        when(serviceRateService.getDefaultServiceRate()).thenReturn(new BigDecimal("2.00"));

        // 创建销户结算信息
        TerminationSettlementDto terminationSettlementDto = new TerminationSettlementDto();
        terminationSettlementDto.setCancelNo("CANCEL-001")
                .setAccountId(1001)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(2001)
                .setOwnerName("测试企业")
                .setMeterIdList(List.of(1001))
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricMeterAmount(1)
                .setFullCancel(false)
                .setCloseReason("test")
                .setSettlementAmount(new BigDecimal("150.00"));

        // 创建销户订单信息
        TerminationOrderCreationInfoDto orderCreationInfo = new TerminationOrderCreationInfoDto();
        orderCreationInfo
                .setTerminationInfo(terminationSettlementDto)
                .setUserId(1002)
                .setUserPhone("13800000002")
                .setUserRealName("销户测试用户")
                .setThirdPartyUserId("termination_user_test")
                .setOrderAmount(new BigDecimal("150.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        // 执行订单创建
        OrderCreationResponseDto result = orderService.createOrder(orderCreationInfo);

        // 验证返回结果
        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isNotBlank();
        assertThat(result.getOrderTypeEnum()).isEqualTo(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT);
        assertThat(result.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);

        // 验证订单已保存到数据库
        OrderBo savedOrder = orderService.getDetail(result.getOrderSn());
        assertThat(savedOrder).isNotNull();

        // 详细验证savedOrder的各个字段
        assertThat(savedOrder.getOrderSn()).isEqualTo(result.getOrderSn());
        assertThat(savedOrder.getUserId()).isEqualTo(1002);
        assertThat(savedOrder.getUserPhone()).isEqualTo("13800000002");
        assertThat(savedOrder.getUserRealName()).isEqualTo("销户测试用户");
        assertThat(savedOrder.getThirdPartyUserId()).isEqualTo("termination_user_test");
        assertThat(savedOrder.getOrderType()).isEqualTo(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT);
        assertThat(savedOrder.getOrderAmount()).isEqualByComparingTo("150.00");
        assertThat(savedOrder.getServiceRate()).isEqualByComparingTo("0.00");
        assertThat(savedOrder.getUserPayAmount()).isEqualByComparingTo("150.00"); // 150.00 + 2.00
        assertThat(savedOrder.getCurrency()).isEqualTo("CNY");
        assertThat(savedOrder.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.NOT_PAY);

        // 验证order_detail_termination表中的数据
        List<OrderDetailTerminationEntity> terminationDetails = orderDetailTerminationRepository.selectList(
                new QueryWrapper<OrderDetailTerminationEntity>()
                        .eq("order_sn", result.getOrderSn())
        );

        assertThat(terminationDetails).isNotEmpty();
        assertThat(terminationDetails).hasSize(1);

        OrderDetailTerminationEntity terminationDetail = terminationDetails.get(0);
        assertThat(terminationDetail.getOrderSn()).isEqualTo(result.getOrderSn());
        assertThat(terminationDetail.getCancelNo()).isEqualTo("CANCEL-001");
        assertThat(terminationDetail.getAccountId()).isEqualTo(1001);
        assertThat(terminationDetail.getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE.getCode());
        assertThat(terminationDetail.getOwnerId()).isEqualTo(2001);
        assertThat(terminationDetail.getOwnerName()).isEqualTo("测试企业");
        assertThat(terminationDetail.getCloseReason()).isEqualTo("test");
        assertThat(terminationDetail.getSettlementAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void createOrder_ShouldThrowException_WhenUnsupportedOrderType() {
        // 创建不支持的订单类型（没有对应的OrderCreationHandler）
        UnsupportedOrderCreationInfoDto orderCreationInfo = new UnsupportedOrderCreationInfoDto();
        orderCreationInfo.setUserId(1001)
                .setUserPhone("13800000002")
                .setUserRealName("测试用户")
                .setThirdPartyUserId("test_user_id")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        // 验证抛出业务异常
        assertThatThrownBy(() -> orderService.createOrder(orderCreationInfo))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("不支持的订单类型");
    }

    private OrderEntity buildOrderEntity(String orderSn) {
        LocalDateTime now = LocalDateTime.now();
        return new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1001)
                .setUserRealName("测试用户")
                .setUserPhone("13800000000")
                .setThirdPartyUserId("third-user-001")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("99.98"))
                .setCurrency("CNY")
                .setServiceRate(BigDecimal.ZERO)
                .setServiceAmount(BigDecimal.ZERO)
                .setUserPayAmount(new BigDecimal("99.98"))
                .setRemark("integration test order")
                .setOrderCreateTime(now)
                .setOrderPayStopTime(now.plusHours(1))
                .setIsDeleted(Boolean.FALSE);
    }

    @Test
    void close_ShouldCloseOrderSuccessfully_WhenOrderExists() {
        // Given - 创建一个未支付的微信小程序订单
        String orderSn = "IT-CLOSE-ORDER-001";
        OrderEntity entity = buildOrderEntity(orderSn)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name());
        orderRepository.insert(entity);

        // Mock微信支付配置和SDK调用
        WxPayConfig config = new WxPayConfig();
        config.setAppId("aaaa");
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(config);

        // Mock微信SDK的queryOrderByOutTradeNo方法返回未关闭状态的Transaction
        when(wxMiniProgramPaySdk.queryOrderByOutTradeNo(any(String.class), any(WxPayConfig.class)))
                .thenReturn(transaction);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.NOTPAY);

        // When - 关闭订单
        orderService.close(orderSn);

        // Then - 验证订单状态已更新为关闭
        OrderBo closedOrder = orderService.getDetail(orderSn);
        assertThat(closedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.CLOSED);
        verify(wxMiniProgramPaySdk, times(1)).closePay(orderSn, config);
    }

    @Test
    void close_ShouldReturnEarly_WhenOrderAlreadyClosed() {
        // Given - 创建一个已关闭的订单
        String orderSn = "IT-CLOSE-ORDER-002";
        OrderEntity entity = buildOrderEntity(orderSn)
                .setOrderStatus(OrderStatusEnum.CLOSED.name())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name());
        orderRepository.insert(entity);

        // When - 尝试关闭已关闭的订单
        orderService.close(orderSn);

        // Then - 验证订单状态仍为关闭（没有异常抛出）
        OrderBo order = orderService.getDetail(orderSn);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatusEnum.CLOSED);
    }

    @Test
    void close_ShouldThrowNotFoundException_WhenOrderNotExists() {
        // Given - 不存在的订单号
        String nonExistentOrderSn = "NON-EXISTENT-ORDER";

        // When & Then - 验证抛出订单不存在异常
        assertThatThrownBy(() -> orderService.close(nonExistentOrderSn))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");
    }

    @Test
    void close_ShouldCloseWxMiniProgramOrder() {
        // Given - 创建微信小程序订单并mock微信SDK
        String orderSn = "IT-CLOSE-WX-ORDER-001";
        OrderEntity entity = buildOrderEntity(orderSn)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name());
        orderRepository.insert(entity);

        WxPayConfig config = new WxPayConfig();
        config.setAppId("aaaa");
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(config);
        when(wxMiniProgramPaySdk.queryOrderByOutTradeNo(any(String.class), any(WxPayConfig.class)))
                .thenReturn(transaction);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.NOTPAY);

        // When - 关闭微信订单
        orderService.close(orderSn);

        // Then - 验证订单状态已更新为关闭
        OrderBo closedOrder = orderService.getDetail(orderSn);
        assertThat(closedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.CLOSED);
        verify(wxMiniProgramPaySdk, times(1)).closePay(orderSn, config);
    }

    @Test
    void close_ShouldThrowBusinessException_WhenOfflineOrder() {
        // Given - 创建一个线下支付订单
        String orderSn = "IT-CLOSE-OFFLINE-ORDER-001";
        OrderEntity entity = buildOrderEntity(orderSn)
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name());
        orderRepository.insert(entity);

        // When & Then - 验证关闭线下订单时抛出业务异常
        assertThatThrownBy(() -> orderService.close(orderSn))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("线下订单默认通过，无法关闭");

        // 验证订单状态未改变
        OrderBo order = orderService.getDetail(orderSn);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatusEnum.NOT_PAY);
    }

    /**
     * 不支持的订单创建信息DTO，用于测试异常场景
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    private static class UnsupportedOrderCreationInfoDto extends OrderCreationInfoDto {
        // 这个类型没有对应的OrderCreationHandler实现
    }

    // ==================== complete方法集成测试 ====================

    @Test
    @Transactional
    void complete_ShouldCompleteWxMiniOrder_WhenValidOrder() {
        // Given
        String orderSn = "TEST_ORDER_33";
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        // 创建对应的OrderDetailEnergyTopUpEntity数据
        OrderDetailEnergyTopUpEntity orderDetailEntity = new OrderDetailEnergyTopUpEntity()
                .setOrderSn(orderSn)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerName("测试企业")
                .setAccountId(2001)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setMeterId(3001)
                .setMeterName("测试电表001")
                .setDeviceNo("EM001")
                .setSpaceId(4001)
                .setSpaceName("测试空间")
                .setSpaceParentIds("1,2,3")
                .setSpaceParentNames("根空间,二级空间,三级空间")
                .setCreateTime(LocalDateTime.now());
        orderDetailEnergyTopUpRepository.insert(orderDetailEntity);

        Transaction transaction = new Transaction();
        TransactionAmount transactionAmount = new TransactionAmount();
        transactionAmount.setCurrency("CNY");
        transactionAmount.setPayerTotal(10000);
        transactionAmount.setTotal(10000);
        transaction.setAmount(transactionAmount);
        transaction.setTradeState(Transaction.TradeStateEnum.SUCCESS);
        transaction.setTransactionId("xxx");
        when(wxMiniProgramPaySdk.queryOrderByOutTradeNo(any(), any())).thenReturn(transaction);

        // When
        orderService.complete(orderSn);

        // Then
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.SUCCESS.name());
        assertThat(updatedOrder.getOrderSuccessTime()).isNotNull();
    }

    @Test
    @Transactional
    void complete_ShouldCompleteOfflineOrder_WhenValidOrder() {
        // Given
        String orderSn = "TEST_ORDER_123";
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name())
                .setOrderCreateTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        // 创建对应的OrderDetailEnergyTopUpEntity数据
        OrderDetailEnergyTopUpEntity orderDetailEntity = new OrderDetailEnergyTopUpEntity()
                .setOrderSn(orderSn)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerName("测试企业")
                .setAccountId(2001)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setMeterId(3001)
                .setMeterName("测试电表001")
                .setDeviceNo("EM001")
                .setSpaceId(4001)
                .setSpaceName("测试空间")
                .setSpaceParentIds("1,2,3")
                .setSpaceParentNames("根空间,二级空间,三级空间")
                .setCreateTime(LocalDateTime.now());
        orderDetailEnergyTopUpRepository.insert(orderDetailEntity);

        // When
        orderService.complete(orderSn);

        // Then
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.SUCCESS.name());
        assertThat(updatedOrder.getOrderSuccessTime()).isNotNull();
    }

    @Test
    @Transactional
    void complete_ShouldReturnEarly_WhenOrderAlreadySuccess() {
        // Given
        String orderSn = "TEST_ORDER_" + System.currentTimeMillis();
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.SUCCESS.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(LocalDateTime.now())
                .setOrderSuccessTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        // When
        orderService.complete(orderSn);

        // Then
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.SUCCESS.name());
    }

    @Test
    @Transactional
    void complete_ShouldReturnEarly_WhenOrderAlreadyClosed() {
        // Given
        String orderSn = "TEST_ORDER_" + System.currentTimeMillis();
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.CLOSED.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        // When
        orderService.complete(orderSn);

        // Then
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.CLOSED.name());
    }

    @Test
    @Transactional
    void complete_ShouldThrowNotFoundException_WhenOrderNotExists() {
        // Given
        String nonExistentOrderSn = "NON_EXISTENT_ORDER";

        // When & Then
        assertThatThrownBy(() -> orderService.complete(nonExistentOrderSn))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");
    }

    @Test
    @Transactional
    void complete_ShouldHandleWxSdkException_WhenCheckCompleteFails() {
        // Given
        String orderSn = "TEST_ORDER_" + System.currentTimeMillis();
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        doThrow(new RuntimeException("微信SDK查询异常"))
                .when(wxMiniProgramPaySdk).queryOrderByOutTradeNo(any(), any());

        // When & Then
        assertThatThrownBy(() -> orderService.complete(orderSn))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("微信SDK查询异常");

        // 验证订单状态未改变
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.NOT_PAY.name());
    }

    // ==================== answerPayNotify方法集成测试 ====================

    @Test
    @Transactional
    void answerPayNotify_ShouldProcessWxMiniNotification_WhenValidNotification() {
        // Given
        String orderSn = "TEST_ORDER_123" ;
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        // 创建对应的OrderDetailEnergyTopUpEntity数据
        OrderDetailEnergyTopUpEntity orderDetailEntity = new OrderDetailEnergyTopUpEntity()
                .setOrderSn(orderSn)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerName("测试企业")
                .setAccountId(2001)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setMeterId(3001)
                .setMeterName("测试电表001")
                .setDeviceNo("EM001")
                .setSpaceId(4001)
                .setSpaceName("测试空间")
                .setSpaceParentIds("1,2,3")
                .setSpaceParentNames("根空间,二级空间,三级空间")
                .setCreateTime(LocalDateTime.now());
        orderDetailEnergyTopUpRepository.insert(orderDetailEntity);

        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData(new RequestParam.Builder().build());

        Transaction transaction = new Transaction();
        transaction.setOutTradeNo(orderSn);
        TransactionAmount transactionAmount = new TransactionAmount();
        transactionAmount.setCurrency("CNY");
        transactionAmount.setTotal(10000);
        transactionAmount.setPayerTotal(10000);
        transaction.setAmount(transactionAmount);
        transaction.setTransactionId("xxx");
        transaction.setTradeState(Transaction.TradeStateEnum.SUCCESS);
        when(wxMiniProgramPaySdk.parseWeiXinNotification(any(), any(), any())).thenReturn(transaction);

        // When
        orderService.answerPayNotify(notification);

        // Then
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.SUCCESS.name());
        assertThat(updatedOrder.getOrderSuccessTime()).isNotNull();
    }

    @Test
    @Transactional
    void answerPayNotify_ShouldProcessOfflineNotification_WhenValidNotification() {
        // Given
        String orderSn = "TEST_ORDER_" + System.currentTimeMillis();
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name())
                .setOrderCreateTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                .setData(new RequestParam.Builder().build());

        // When
        assertThatThrownBy(() -> orderService.answerPayNotify(notification))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("线下订单没有回调通知");

        // Then
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.NOT_PAY.name());
        assertThat(updatedOrder.getOrderSuccessTime()).isNull();
    }

    @Test
    @Transactional
    void answerPayNotify_ShouldReturnEarly_WhenOrderAlreadySuccess() {
        // Given
        String orderSn = "TEST_ORDER_" + System.currentTimeMillis();
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.SUCCESS.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(LocalDateTime.now())
                .setOrderSuccessTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData(new RequestParam.Builder().build());

        Transaction transaction = new Transaction();
        transaction.setOutTradeNo(orderSn);
        TransactionAmount transactionAmount = new TransactionAmount();
        transactionAmount.setCurrency("CNY");
        transactionAmount.setTotal(10000);
        transactionAmount.setPayerTotal(10000);
        transaction.setAmount(transactionAmount);
        transaction.setTransactionId("xxx");
        transaction.setTradeState(Transaction.TradeStateEnum.SUCCESS);
        when(wxMiniProgramPaySdk.parseWeiXinNotification(any(), any(), any())).thenReturn(transaction);

        // When
        orderService.answerPayNotify(notification);

        // Then
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.SUCCESS.name());
    }

    @Test
    @Transactional
    void answerPayNotify_ShouldReturnEarly_WhenOrderAlreadyClosed() {
        // Given
        String orderSn = "TEST_ORDER_" + System.currentTimeMillis();
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setOrderStatus(OrderStatusEnum.CLOSED.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData(new RequestParam.Builder().build());

        Transaction transaction = new Transaction();
        transaction.setOutTradeNo(orderSn);
        TransactionAmount transactionAmount = new TransactionAmount();
        transactionAmount.setCurrency("CNY");
        transactionAmount.setTotal(10000);
        transactionAmount.setPayerTotal(10000);
        transaction.setAmount(transactionAmount);
        transaction.setTransactionId("xxx");
        transaction.setTradeState(Transaction.TradeStateEnum.CLOSED);
        when(wxMiniProgramPaySdk.parseWeiXinNotification(any(), any(), any())).thenReturn(transaction);

        // When
        orderService.answerPayNotify(notification);

        // Then
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.CLOSED.name());
    }

    @Test
    @Transactional
    void answerPayNotify_PayAmountError_WhenPaymentAmountMismatch() {
        // Given
        String orderSn = "TEST_ORDER_" + System.currentTimeMillis();
        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setUserRealName("测试用户")
                .setUserPhone("12345678901")
                .setThirdPartyUserId("abc")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("50.00")) // 设置用户实付金额与订单金额不一致
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderCreateTime(LocalDateTime.now());
        orderRepository.insert(orderEntity);

        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData(new RequestParam.Builder().build());


        Transaction transaction = new Transaction();
        transaction.setOutTradeNo(orderSn);
        TransactionAmount transactionAmount = new TransactionAmount();
        transactionAmount.setCurrency("CNY");
        transactionAmount.setTotal(10000);
        transactionAmount.setPayerTotal(10000);
        transaction.setAmount(transactionAmount);
        transaction.setTransactionId("xxx");
        transaction.setTradeState(Transaction.TradeStateEnum.CLOSED);
        when(wxMiniProgramPaySdk.parseWeiXinNotification(any(), any(), any())).thenReturn(transaction);


        // When & Then
        orderService.answerPayNotify(notification);

        // 验证订单状态未改变
        OrderEntity updatedOrder = orderRepository.selectByOrderSn(orderSn);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.PAY_ERROR.name());
    }

    @Test
    @Transactional
    void answerPayNotify_ShouldThrowNotFoundException_WhenOrderNotExists() {
        // Given
        String nonExistentOrderSn = "NON_EXISTENT_ORDER";
        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData(new RequestParam.Builder().build());

        Transaction transaction = new Transaction();
        transaction.setOutTradeNo(nonExistentOrderSn);
        TransactionAmount transactionAmount = new TransactionAmount();
        transactionAmount.setCurrency("CNY");
        transactionAmount.setTotal(10000);
        transactionAmount.setPayerTotal(10000);
        transaction.setAmount(transactionAmount);
        transaction.setTransactionId("xxx");
        transaction.setTradeState(Transaction.TradeStateEnum.CLOSED);
        when(wxMiniProgramPaySdk.parseWeiXinNotification(any(), any(), any())).thenReturn(transaction);

        // When & Then
        assertThatThrownBy(() -> orderService.answerPayNotify(notification))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");
    }
}
