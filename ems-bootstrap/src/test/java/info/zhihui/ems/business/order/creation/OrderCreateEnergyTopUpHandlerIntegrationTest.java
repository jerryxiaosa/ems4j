package info.zhihui.ems.business.order.creation;

import info.zhihui.ems.business.order.bo.OrderBo;
import info.zhihui.ems.business.order.dto.ServiceFeeDto;
import info.zhihui.ems.business.order.dto.ServiceFeeRequestDto;
import info.zhihui.ems.business.order.dto.creation.EnergyOrderCreationInfoDto;
import info.zhihui.ems.business.order.dto.creation.EnergyTopUpDto;
import info.zhihui.ems.business.order.entity.OrderDetailEnergyTopUpEntity;
import info.zhihui.ems.business.order.entity.OrderEntity;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.business.order.enums.OrderStatusEnum;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.order.enums.PaymentChannelEnum;
import info.zhihui.ems.business.order.repository.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.order.repository.OrderRepository;
import info.zhihui.ems.business.order.service.fee.ServiceRateService;
import info.zhihui.ems.business.order.service.handler.impl.OrderEnergyTopUpHandler;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import org.junit.jupiter.api.AfterEach;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderCreateEnergyTopUpHandler 集成测试
 *
 * @author jerryxiaosa
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("integrationtest")
class OrderCreateEnergyTopUpHandlerIntegrationTest {

    @Autowired
    private OrderEnergyTopUpHandler orderCreateEnergyTopUpHandler;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;

    @Autowired
    private ServiceRateService serviceRateService;

    private EnergyTopUpDto energyTopUpDto;
    private EnergyOrderCreationInfoDto energyOrderCreationInfoDto;
    private BigDecimal originServiceRate;

    @BeforeEach
    void setUp() {
        originServiceRate = serviceRateService.getDefaultServiceRate();
        // 初始化测试DTO
        energyTopUpDto = new EnergyTopUpDto()
                .setAccountId(1)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterId(200)
                .setMeterType(MeterTypeEnum.ELECTRIC)
                .setMeterName("测试电表")
                .setDeviceNo("METER001")
                .setSpaceId(300);

        // 初始化订单创建信息DTO
        energyOrderCreationInfoDto = new EnergyOrderCreationInfoDto();
        energyOrderCreationInfoDto
                .setEnergyTopUpDto(energyTopUpDto)
                .setUserId(1)
                .setUserPhone("13911100111")
                .setUserRealName("test-user")
                .setThirdPartyUserId("abc")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);
    }

    @AfterEach
    void tearDown() {
        serviceRateService.updateDefaultServiceRate(originServiceRate);
    }


    @Test
    @DisplayName("正常创建订单测试 - 应成功创建订单和订单详情")
    @Transactional
    void testHandle_NormalOrder_ShouldCreateOrderAndDetail() {
        // 执行订单创建
        OrderBo orderBo = assertDoesNotThrow(() -> orderCreateEnergyTopUpHandler.createOrder(energyOrderCreationInfoDto));

        assertNotNull(orderBo, "应该返回订单BO");
        assertNotNull(orderBo.getOrderSn(), "订单号不应为空");
        String start = LocalDate.now().getYear() + "01";
        assertTrue(orderBo.getOrderSn().startsWith(start), "订单号应该以" + start + "开头");

        // 验证订单主表数据
        List<OrderEntity> orders = orderRepository.selectList(null);
        OrderEntity savedOrder = orders.stream()
                .filter(order -> order.getOrderSn().equals(orderBo.getOrderSn()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedOrder, "应该保存订单主表记录");
        assertEquals(orderBo.getOrderSn(), savedOrder.getOrderSn());
        assertEquals(energyOrderCreationInfoDto.getUserId(), savedOrder.getUserId());
        assertEquals(OrderTypeEnum.ENERGY_TOP_UP.getCode(), savedOrder.getOrderType());
        assertEquals(energyOrderCreationInfoDto.getOrderAmount(), savedOrder.getOrderAmount().setScale(2, RoundingMode.DOWN));
        assertEquals(new BigDecimal("0.05"), savedOrder.getServiceRate().setScale(2, RoundingMode.DOWN));
        assertEquals(new BigDecimal("5.00"), savedOrder.getServiceAmount().setScale(2, RoundingMode.DOWN));
        assertEquals(new BigDecimal("100.00"), savedOrder.getUserPayAmount().setScale(2, RoundingMode.DOWN));
        assertEquals(OrderStatusEnum.NOT_PAY.name(), savedOrder.getOrderStatus());
        assertNotNull(savedOrder.getOrderCreateTime());
        assertNotNull(savedOrder.getOrderPayStopTime());

        // 验证订单详情表数据
        List<OrderDetailEnergyTopUpEntity> orderDetails = orderDetailEnergyTopUpRepository.selectList(null);
        OrderDetailEnergyTopUpEntity savedOrderDetail = orderDetails.stream()
                .filter(detail -> detail.getOrderSn().equals(orderBo.getOrderSn()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedOrderDetail, "应该保存订单详情记录");
        assertEquals(orderBo.getOrderSn(), savedOrderDetail.getOrderSn());
        assertEquals(energyTopUpDto.getOwnerId(), savedOrderDetail.getOwnerId());
        assertEquals(energyTopUpDto.getAccountId(), savedOrderDetail.getAccountId());
        assertEquals(energyTopUpDto.getOwnerType().getCode(), savedOrderDetail.getOwnerType());
        assertEquals(energyTopUpDto.getElectricAccountType().getCode(), savedOrderDetail.getElectricAccountType());
        assertEquals(energyTopUpDto.getMeterId(), savedOrderDetail.getMeterId());
        assertEquals(0, savedOrderDetail.getTopUpAmount().compareTo(new BigDecimal("95.00000000")));
    }

    @Test
    @DisplayName("参数校验测试 - 缺少必填参数应抛出异常")
    @Transactional
    void testHandle_MissingRequiredFields_ShouldThrowException() {
        // 测试缺少accountId
        EnergyTopUpDto invalidDto1 = new EnergyTopUpDto()
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(100)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterId(200);

        EnergyOrderCreationInfoDto invalidOrderInfo1 = new EnergyOrderCreationInfoDto();
        invalidOrderInfo1.setEnergyTopUpDto(invalidDto1)
                .setUserId(1)
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        assertThrows(ConstraintViolationException.class, () -> {
            orderCreateEnergyTopUpHandler.createOrder(invalidOrderInfo1);
        }, "缺少accountId应该抛出校验异常");

    }

    @Test
    @DisplayName("参数校验测试 - 订单总金额超过两位小数应抛出异常")
    void testHandle_OrderAmountHasMoreThanTwoFractionDigits_ShouldThrowException() {
        energyOrderCreationInfoDto.setOrderAmount(new BigDecimal("100.009"));

        assertThrows(ConstraintViolationException.class, () ->
                orderCreateEnergyTopUpHandler.createOrder(energyOrderCreationInfoDto));
    }

    @Test
    @DisplayName("服务费计算测试 - 应正确计算服务费")
    void testGetServiceFee_NormalAmount_ShouldCalculateCorrectly() {
        ServiceFeeDto serviceFee = orderCreateEnergyTopUpHandler.getServiceFee(new ServiceFeeRequestDto()
                .setOrderOriginalAmount(energyOrderCreationInfoDto.getOrderAmount())
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
        );


        assertNotNull(serviceFee, "服务费不应为null");
        assertTrue(serviceFee.getServiceAmount().compareTo(BigDecimal.ZERO) >= 0, "服务费应该大于等于0");
        assertEquals(new BigDecimal("0.05"), serviceFee.getServiceRate().setScale(2, RoundingMode.DOWN), "服务费率应该正确");
        assertEquals(new BigDecimal("5.00"), serviceFee.getServiceAmount().setScale(2, RoundingMode.DOWN), "服务费计算应该正确");
        assertEquals(energyOrderCreationInfoDto.getOrderAmount(), serviceFee.getUserPayAmount(), "用户实付金额应该等于订单总金额");
    }

    @Test
    @DisplayName("服务费计算测试 - 服务费率为0时服务费应为0")
    void testGetServiceFee_WhenServiceRateIsZero_ShouldReturnZeroServiceAmount() {
        serviceRateService.updateDefaultServiceRate(BigDecimal.ZERO);
        ServiceFeeDto serviceFee = orderCreateEnergyTopUpHandler.getServiceFee(new ServiceFeeRequestDto()
                .setOrderOriginalAmount(new BigDecimal("100.00"))
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
        );

        assertNotNull(serviceFee, "服务费结果不应为null");
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.DOWN), serviceFee.getServiceRate().setScale(2, RoundingMode.DOWN));
        assertEquals(new BigDecimal("0.00"), serviceFee.getServiceAmount().setScale(2, RoundingMode.DOWN), "服务费应为0");
        assertEquals(new BigDecimal("100.00"), serviceFee.getUserPayAmount().setScale(2, RoundingMode.DOWN), "用户实付金额应保持为订单总金额");
    }

    @Test
    @DisplayName("服务费计算测试 - 服务费大于0时最小应为0.01")
    void testGetServiceFee_WhenCalculatedAmountLessThanMin_ShouldUseMinServiceAmount() {
        serviceRateService.updateDefaultServiceRate(new BigDecimal("0.05"));
        ServiceFeeDto serviceFee = orderCreateEnergyTopUpHandler.getServiceFee(new ServiceFeeRequestDto()
                .setOrderOriginalAmount(new BigDecimal("0.10"))
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
        );

        assertNotNull(serviceFee, "服务费结果不应为null");
        assertEquals(new BigDecimal("0.05"), serviceFee.getServiceRate().setScale(2, RoundingMode.DOWN), "服务费率应为配置值");
        assertEquals(new BigDecimal("0.01"), serviceFee.getServiceAmount().setScale(2, RoundingMode.DOWN), "服务费应取最小值0.01");
        assertEquals(new BigDecimal("0.10"), serviceFee.getUserPayAmount().setScale(2, RoundingMode.DOWN), "用户实付金额应保持为订单总金额");
    }

    @Test
    @DisplayName("创建订单测试 - 订单金额过小导致到账金额小于等于0应抛异常")
    void testHandle_WhenOrderAmountTooSmallAfterFee_ShouldThrowException() {
        serviceRateService.updateDefaultServiceRate(new BigDecimal("0.05"));
        energyOrderCreationInfoDto.setOrderAmount(new BigDecimal("0.01"));

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () ->
                orderCreateEnergyTopUpHandler.createOrder(energyOrderCreationInfoDto));
        assertEquals("订单金额过小，扣除服务费后实际充值到账金额必须大于0", exception.getMessage());
    }

    @Test
    @DisplayName("多次创建订单测试 - 应生成不同的订单号")
    @Transactional
    void testHandle_MultipleOrders_ShouldGenerateDifferentOrderSn() {
        // 创建第一个订单
        OrderBo orderBo1 = orderCreateEnergyTopUpHandler.createOrder(energyOrderCreationInfoDto);

        // 创建第二个订单
        EnergyTopUpDto secondDto = new EnergyTopUpDto()
                .setAccountId(2)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(101)
                .setOwnerName("测试个人")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMeterId(201)
                .setMeterType(MeterTypeEnum.ELECTRIC)
                .setMeterName("测试电表2")
                .setDeviceNo("METER002")
                .setSpaceId(301);

        EnergyOrderCreationInfoDto secondOrderInfo = new EnergyOrderCreationInfoDto();
        secondOrderInfo.setEnergyTopUpDto(secondDto)
                .setUserId(2)
                .setUserPhone("139444")
                .setUserRealName("test-user")
                .setThirdPartyUserId("aaa")
                .setOrderAmount(new BigDecimal("200.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI);

        OrderBo orderBo2 = orderCreateEnergyTopUpHandler.createOrder(secondOrderInfo);

        assertNotEquals(orderBo1.getOrderSn(), orderBo2.getOrderSn(), "不同订单应该有不同的订单号");

        // 验证两个订单都正确保存
        List<OrderEntity> orders = orderRepository.selectList(null);
        long matchingOrders = orders.stream()
                .filter(order -> order.getOrderSn().equals(orderBo1.getOrderSn()) || order.getOrderSn().equals(orderBo2.getOrderSn()))
                .count();
        assertEquals(2, matchingOrders, "应该保存两个订单记录");

        List<OrderDetailEnergyTopUpEntity> orderDetails = orderDetailEnergyTopUpRepository.selectList(null);
        long matchingDetails = orderDetails.stream()
                .filter(detail -> detail.getOrderSn().equals(orderBo1.getOrderSn()) || detail.getOrderSn().equals(orderBo2.getOrderSn()))
                .count();
        assertEquals(2, matchingDetails, "应该保存两个订单详情记录");
    }

    @Test
    @DisplayName("不同电表账户类型测试 - 应正确处理不同的电表账户类型")
    @Transactional
    void testHandle_DifferentElectricAccountTypes_ShouldHandleCorrectly() {
        // 测试计量账户
        EnergyTopUpDto quantityDto = new EnergyTopUpDto()
                .setAccountId(1)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterId(200)
                .setMeterType(MeterTypeEnum.ELECTRIC)
                .setMeterName("测试电表")
                .setDeviceNo("METER001")
                .setSpaceId(300);

        EnergyOrderCreationInfoDto quantityOrderInfo = new EnergyOrderCreationInfoDto();
        quantityOrderInfo.setEnergyTopUpDto(quantityDto)
                .setUserId(1)
                .setUserRealName("test-user")
                .setUserPhone("139111")
                .setThirdPartyUserId("abc")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        OrderBo quantityOrderBo = orderCreateEnergyTopUpHandler.createOrder(quantityOrderInfo);
        assertNotNull(quantityOrderBo.getOrderSn(), "计量账户订单应该创建成功");

        // 测试月结账户
        EnergyTopUpDto monthlyDto = new EnergyTopUpDto()
                .setAccountId(2)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(2001)
                .setOwnerName("测试企业2")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMeterId(201)
                .setMeterType(MeterTypeEnum.ELECTRIC)
                .setMeterName("测试电表2")
                .setDeviceNo("METER002")
                .setSpaceId(301);

        EnergyOrderCreationInfoDto monthlyOrderInfo = new EnergyOrderCreationInfoDto();
        monthlyOrderInfo.setEnergyTopUpDto(monthlyDto)
                .setUserId(2)
                .setUserPhone("133")
                .setUserRealName("test-user2")
                .setThirdPartyUserId("aaabb")
                .setOrderAmount(new BigDecimal("150.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI);

        OrderBo monthlyOrderBo = orderCreateEnergyTopUpHandler.createOrder(monthlyOrderInfo);
        assertNotNull(monthlyOrderBo.getOrderSn(), "月结账户订单应该创建成功");

        // 验证订单详情中的电表账户类型
        List<OrderDetailEnergyTopUpEntity> orderDetails = orderDetailEnergyTopUpRepository.selectList(null);
        OrderDetailEnergyTopUpEntity quantityDetail = orderDetails.stream()
                .filter(detail -> detail.getOrderSn().equals(quantityOrderBo.getOrderSn()))
                .findFirst()
                .orElse(null);

        assertNotNull(quantityDetail, "应该保存计量账户订单详情");
        assertEquals(ElectricAccountTypeEnum.QUANTITY.getCode(), quantityDetail.getElectricAccountType());

        OrderDetailEnergyTopUpEntity monthlyDetail = orderDetails.stream()
                .filter(detail -> detail.getOrderSn().equals(monthlyOrderBo.getOrderSn()))
                .findFirst()
                .orElse(null);

        assertNotNull(monthlyDetail, "应该保存月结账户订单详情");
        assertEquals(ElectricAccountTypeEnum.MONTHLY.getCode(), monthlyDetail.getElectricAccountType());
    }
}
