package info.zhihui.ems.business.finance.order.creation;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationOrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationSettlementDto;
import info.zhihui.ems.business.finance.entity.order.OrderDetailTerminationEntity;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.repository.order.OrderDetailTerminationRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.handler.impl.OrderTerminationHandler;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
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
 * OrderCreationTerminationHandler 集成测试
 *
 * @author jerryxiaosa
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("integrationtest")
class OrderTerminationHandlerIntegrationTest {

    @Autowired
    private OrderTerminationHandler orderTerminationHandler;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailTerminationRepository orderDetailTerminationRepository;

    private TerminationSettlementDto terminationSettlementDto;
    private TerminationOrderCreationInfoDto terminationOrderCreationInfoDto;

    @BeforeEach
    void setUp() {
        // 初始化销户结算DTO
        terminationSettlementDto = new TerminationSettlementDto()
                .setCancelNo("CANCEL001")
                .setAccountId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setOwnerName("测试企业")
                .setElectricMeterAmount(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterIdList(List.of(30))
                .setFullCancel(false)
                .setSettlementAmount(new BigDecimal("100.00"))
                .setCloseReason("test");

        // 初始化销户订单创建信息DTO
        terminationOrderCreationInfoDto = new TerminationOrderCreationInfoDto();
        terminationOrderCreationInfoDto
                .setTerminationInfo(terminationSettlementDto)
                .setUserId(1)
                .setUserPhone("13911100111")
                .setUserRealName("test-user")
                .setThirdPartyUserId("abc")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);
    }

    @Test
    @DisplayName("正常创建销户订单测试 - 应成功创建订单和订单详情")
    @Transactional
    void testCreateOrder_NormalTerminationOrder_ShouldCreateOrderAndDetail() {
        // 执行订单创建
        OrderBo orderBo = assertDoesNotThrow(() -> orderTerminationHandler.createOrder(terminationOrderCreationInfoDto));

        assertNotNull(orderBo, "应该返回订单BO");
        assertNotNull(orderBo.getOrderSn(), "订单号不应为空");
        String start = LocalDate.now().getYear() + "02";
        assertTrue(orderBo.getOrderSn().startsWith(start), "订单号应该以" + start + "开头");

        // 验证订单主表数据
        List<OrderEntity> orders = orderRepository.selectList(null);
        OrderEntity savedOrder = orders.stream()
                .filter(order -> order.getOrderSn().equals(orderBo.getOrderSn()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedOrder, "应该保存订单主表记录");
        assertEquals(orderBo.getOrderSn(), savedOrder.getOrderSn());
        assertEquals(terminationOrderCreationInfoDto.getUserId(), savedOrder.getUserId());
        assertEquals(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT.getCode(), savedOrder.getOrderType());
        assertEquals(terminationOrderCreationInfoDto.getOrderAmount(), savedOrder.getOrderAmount().setScale(2, RoundingMode.DOWN));
        assertEquals(OrderStatusEnum.NOT_PAY.name(), savedOrder.getOrderStatus());
        assertNotNull(savedOrder.getOrderCreateTime());
        assertNotNull(savedOrder.getOrderPayStopTime());

        // 验证订单详情表数据
        List<OrderDetailTerminationEntity> orderDetails = orderDetailTerminationRepository.selectList(null);
        OrderDetailTerminationEntity savedOrderDetail = orderDetails.stream()
                .filter(detail -> detail.getOrderSn().equals(orderBo.getOrderSn()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedOrderDetail, "应该保存订单详情记录");
        assertEquals(orderBo.getOrderSn(), savedOrderDetail.getOrderSn());
        assertEquals(terminationSettlementDto.getCancelNo(), savedOrderDetail.getCancelNo());
        assertEquals(terminationSettlementDto.getAccountId(), savedOrderDetail.getAccountId());
        assertEquals(terminationSettlementDto.getOwnerType().getCode(), savedOrderDetail.getOwnerType());
        assertEquals(terminationSettlementDto.getOwnerId(), savedOrderDetail.getOwnerId());
        assertEquals(terminationSettlementDto.getOwnerName(), savedOrderDetail.getOwnerName());
        assertEquals(terminationSettlementDto.getSettlementAmount(), savedOrderDetail.getSettlementAmount().setScale(2, RoundingMode.DOWN));
        assertEquals(terminationSettlementDto.getCloseReason(), savedOrderDetail.getCloseReason());
    }

    @Test
    @DisplayName("参数校验测试 - 缺少必填参数应抛出异常")
    @Transactional
    void testCreateOrder_MissingRequiredFields_ShouldThrowException() {
        // 测试缺少cancelNo
        TerminationSettlementDto invalidDto1 = new TerminationSettlementDto()
                .setAccountId(1)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(100)
                .setSettlementAmount(new BigDecimal("100.00"));

        TerminationOrderCreationInfoDto invalidOrderInfo1 = new TerminationOrderCreationInfoDto();
        invalidOrderInfo1.setTerminationInfo(invalidDto1)
                .setUserId(1)
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        assertThrows(ConstraintViolationException.class, () -> {
            orderTerminationHandler.createOrder(invalidOrderInfo1);
        }, "缺少cancelNo应该抛出校验异常");

        // 测试缺少accountId
        TerminationSettlementDto invalidDto2 = new TerminationSettlementDto()
                .setCancelNo("CANCEL002")
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(100)
                .setSettlementAmount(new BigDecimal("100.00"));

        TerminationOrderCreationInfoDto invalidOrderInfo2 = new TerminationOrderCreationInfoDto();
        invalidOrderInfo2.setTerminationInfo(invalidDto2)
                .setUserId(1)
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        assertThrows(ConstraintViolationException.class, () -> {
            orderTerminationHandler.createOrder(invalidOrderInfo2);
        }, "缺少accountId应该抛出校验异常");
    }

    @Test
    @DisplayName("多次创建订单测试 - 应生成不同的订单号")
    @Transactional
    void testCreateOrder_MultipleOrders_ShouldGenerateDifferentOrderSn() {
        // 创建第一个订单
        OrderBo orderBo1 = orderTerminationHandler.createOrder(terminationOrderCreationInfoDto);

        // 创建第二个订单
        TerminationSettlementDto secondDto = new TerminationSettlementDto()
                .setCancelNo("CANCEL002")
                .setAccountId(2)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(101)
                .setOwnerName("测试个人")
                .setElectricMeterAmount(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterIdList(List.of(30))
                .setFullCancel(false)
                .setSettlementAmount(new BigDecimal("100.00"))
                .setSettlementAmount(new BigDecimal("200.00"))
                .setCloseReason("test_multi");

        TerminationOrderCreationInfoDto secondOrderInfo = new TerminationOrderCreationInfoDto();
        secondOrderInfo.setTerminationInfo(secondDto)
                .setUserId(2)
                .setUserPhone("139444")
                .setUserRealName("test-user2")
                .setThirdPartyUserId("def")
                .setOrderAmount(new BigDecimal("200.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI);

        OrderBo orderBo2 = orderTerminationHandler.createOrder(secondOrderInfo);

        assertNotEquals(orderBo1.getOrderSn(), orderBo2.getOrderSn(), "不同订单应该有不同的订单号");

        // 验证两个订单都正确保存
        List<OrderEntity> orders = orderRepository.selectList(null);
        long matchingOrders = orders.stream()
                .filter(order -> order.getOrderSn().equals(orderBo1.getOrderSn()) || order.getOrderSn().equals(orderBo2.getOrderSn()))
                .count();
        assertEquals(2, matchingOrders, "应该保存两个订单记录");

        List<OrderDetailTerminationEntity> orderDetails = orderDetailTerminationRepository.selectList(null);
        long matchingDetails = orderDetails.stream()
                .filter(detail -> detail.getOrderSn().equals(orderBo1.getOrderSn()) || detail.getOrderSn().equals(orderBo2.getOrderSn()))
                .count();
        assertEquals(2, matchingDetails, "应该保存两个订单详情记录");
    }

    @Test
    @DisplayName("不同业主类型测试 - 应正确处理不同的业主类型")
    @Transactional
    void testHandle_DifferentOwnerTypes_ShouldCreateOrderCorrectly() {
        // 测试企业类型
        TerminationSettlementDto enterpriseDto = new TerminationSettlementDto()
                .setCancelNo("CANCEL003")
                .setAccountId(3)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(2001)
                .setOwnerName("测试企业2")
                .setSettlementAmount(new BigDecimal("300.00"))
                .setElectricMeterAmount(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterIdList(List.of(30))
                .setFullCancel(false)
                .setSettlementAmount(new BigDecimal("300.00"))
                .setCloseReason("different_Owner_type");

        TerminationOrderCreationInfoDto enterpriseOrderInfo = new TerminationOrderCreationInfoDto();
        enterpriseOrderInfo.setTerminationInfo(enterpriseDto)
                .setUserId(3)
                .setUserPhone("139111")
                .setUserRealName("test-user3")
                .setThirdPartyUserId("ghi")
                .setOrderAmount(new BigDecimal("300.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        OrderBo enterpriseOrderBo = orderTerminationHandler.createOrder(enterpriseOrderInfo);
        assertNotNull(enterpriseOrderBo.getOrderSn(), "企业类型订单应该创建成功");

        // 测试个人类型
        TerminationSettlementDto personalDto = new TerminationSettlementDto()
                .setCancelNo("CANCEL004")
                .setAccountId(4)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(103)
                .setOwnerName("测试个人2")
                .setElectricMeterAmount(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterIdList(List.of(30))
                .setFullCancel(false)
                .setSettlementAmount(new BigDecimal("150.00"))
                .setCloseReason("个人销户原因");

        TerminationOrderCreationInfoDto personalOrderInfo = new TerminationOrderCreationInfoDto();
        personalOrderInfo.setTerminationInfo(personalDto)
                .setUserId(4)
                .setUserPhone("133")
                .setUserRealName("test-user4")
                .setThirdPartyUserId("jkl")
                .setOrderAmount(new BigDecimal("150.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI);

        OrderBo personalOrderBo = orderTerminationHandler.createOrder(personalOrderInfo);
        assertNotNull(personalOrderBo.getOrderSn(), "个人类型订单应该创建成功");

        // 验证订单详情中的业主类型
        List<OrderDetailTerminationEntity> orderDetails = orderDetailTerminationRepository.selectList(null);
        OrderDetailTerminationEntity enterpriseDetail = orderDetails.stream()
                .filter(detail -> detail.getOrderSn().equals(enterpriseOrderBo.getOrderSn()))
                .findFirst()
                .orElse(null);

        assertNotNull(enterpriseDetail, "应该保存企业类型订单详情");
        assertEquals(OwnerTypeEnum.ENTERPRISE.getCode(), enterpriseDetail.getOwnerType());

        OrderDetailTerminationEntity personalDetail = orderDetails.stream()
                .filter(detail -> detail.getOrderSn().equals(personalOrderBo.getOrderSn()))
                .findFirst()
                .orElse(null);

        assertNotNull(personalDetail, "应该保存个人类型订单详情");
        assertEquals(OwnerTypeEnum.PERSONAL.getCode(), personalDetail.getOwnerType());
    }

    @Test
    @DisplayName("订单金额与结算金额不匹配测试 - 应抛出异常")
    @Transactional
    void testCreateOrder_AmountMismatch_ShouldThrowException() {
        // 设置订单金额与结算金额不匹配
        terminationOrderCreationInfoDto.setOrderAmount(new BigDecimal("200.00")); // 订单金额200
        terminationSettlementDto.setSettlementAmount(new BigDecimal("100.00")); // 结算金额100

        assertThrows(RuntimeException.class, () -> {
            orderTerminationHandler.createOrder(terminationOrderCreationInfoDto);
        }, "订单金额与结算金额不匹配应该抛出异常");
    }
}