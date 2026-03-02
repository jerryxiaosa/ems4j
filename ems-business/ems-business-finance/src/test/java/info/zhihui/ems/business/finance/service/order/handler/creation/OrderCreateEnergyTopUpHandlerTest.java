package info.zhihui.ems.business.finance.service.order.handler.creation;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.creation.EnergyOrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.EnergyTopUpDto;
import info.zhihui.ems.business.finance.entity.order.OrderDetailEnergyTopUpEntity;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.fee.ServiceRateService;
import info.zhihui.ems.business.finance.service.order.handler.impl.OrderEnergyTopUpHandler;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.verify;

/**
 * OrderCreateEnergyTopUpHandler单元测试
 *
 * @author Generated
 */
@ExtendWith(MockitoExtension.class)
class OrderCreateEnergyTopUpHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;

    @Mock
    private SpaceService spaceService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ServiceRateService serviceRateService;

    @InjectMocks
    private OrderEnergyTopUpHandler handler;

    // 测试数据
    private EnergyOrderCreationInfoDto energyTopUpDto;
    private SpaceBo spaceBo;

    @BeforeEach
    void setUp() {
        // 初始化充值订单DTO
        EnergyTopUpDto businessDetail = new EnergyTopUpDto();
        businessDetail.setAccountId(1);
        businessDetail.setOwnerType(OwnerTypeEnum.ENTERPRISE);
        businessDetail.setOwnerId(1);
        businessDetail.setOwnerName("测试用户");
        businessDetail.setSpaceId(1);
        businessDetail.setMeterId(100);
        businessDetail.setMeterName("测试电表");
        businessDetail.setDeviceNo("METER001");

        energyTopUpDto = new EnergyOrderCreationInfoDto();
        energyTopUpDto.setUserId(1);
        energyTopUpDto.setUserRealName("testuser");
        energyTopUpDto.setUserPhone("13800138000");
        energyTopUpDto.setThirdPartyUserId("abc");
        energyTopUpDto.setOrderAmount(new BigDecimal("100.00"));
        energyTopUpDto.setPaymentChannel(PaymentChannelEnum.OFFLINE);
        energyTopUpDto.setEnergyTopUpDto(businessDetail);


        // 初始化空间信息
        spaceBo = new SpaceBo();
        spaceBo.setId(1);
        spaceBo.setName("测试空间");
        spaceBo.setOwnAreaId(1);
        spaceBo.setParentsIds(List.of(0, 1));
        spaceBo.setParentsNames(List.of("根节点", "测试空间"));
    }

    @Test
    void testSupports_WhenOrderTypeIsEnergyTopUp_ShouldReturnTrue() {
        // Then
        assertEquals(EnergyOrderCreationInfoDto.class, handler.getSupportedParam(), "应该支持能源充值订单类型");
    }

    @Test
    void testHandle_WhenPersonalUserCreateOrder_ShouldCreateOrderSuccessfully() {
        // Given
        // 模拟依赖服务的行为
        when(spaceService.getDetail(anyInt())).thenReturn(spaceBo);
        when(orderRepository.insert(any(OrderEntity.class))).thenReturn(1);
        when(orderDetailEnergyTopUpRepository.insert(any(OrderDetailEnergyTopUpEntity.class))).thenReturn(1);
        when(orderMapper.toBo(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            return new OrderBo()
                    .setOrderSn(entity.getOrderSn())
                    .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                    .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                    .setOrderStatus(OrderStatusEnum.NOT_PAY)
                    .setOrderAmount(entity.getOrderAmount());
        });
        when(serviceRateService.getDefaultServiceRate()).thenReturn(new BigDecimal("1.00"));

        // When
        OrderBo result = handler.createOrder(energyTopUpDto);

        // Then
        assertNotNull(result, "返回的订单不应为null");
        assertNotNull(result.getOrderSn(), "订单号不应为null");
        assertEquals(new BigDecimal("100.00"), result.getOrderAmount(), "订单金额应该正确");
        assertEquals(OrderTypeEnum.ENERGY_TOP_UP, result.getOrderType(), "订单类型应该正确");
        assertEquals(OrderStatusEnum.NOT_PAY, result.getOrderStatus(), "订单状态应该为未支付");

        // 验证方法调用
        verify(spaceService).getDetail(anyInt());
        verify(orderRepository).insert(any(OrderEntity.class));
        verify(orderDetailEnergyTopUpRepository).insert(any(OrderDetailEnergyTopUpEntity.class));

        // 验证保存的订单数据 - 详细字段校验
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).insert(orderCaptor.capture());
        OrderEntity capturedOrder = orderCaptor.getValue();

        // 基本订单信息校验
        assertNotNull(capturedOrder.getOrderSn(), "订单号不应为null");
        String start = LocalDate.now().getYear() + "01";
        assertTrue(capturedOrder.getOrderSn().startsWith(start), "能源充值订单号应以" + start + "开头");
        assertEquals(1, capturedOrder.getUserId(), "用户ID应该正确");
        assertEquals("testuser", capturedOrder.getUserRealName(), "用户真实姓名应该正确");
        assertEquals("13800138000", capturedOrder.getUserPhone(), "用户手机号应该正确");
        assertEquals(OrderTypeEnum.ENERGY_TOP_UP.getCode(), capturedOrder.getOrderType(), "订单类型应该正确");
        assertEquals(new BigDecimal("100.00"), capturedOrder.getOrderAmount(), "订单金额应该正确");

        // 服务费相关校验
        assertEquals(new BigDecimal("1.00"), capturedOrder.getServiceRate().setScale(2, RoundingMode.DOWN), "服务费率应该正确");
        assertEquals(new BigDecimal("100.00"), capturedOrder.getServiceAmount().setScale(2, RoundingMode.DOWN), "服务费金额应该正确");
        assertEquals(new BigDecimal("200.00"), capturedOrder.getUserPayAmount().setScale(2, RoundingMode.DOWN), "用户实付金额应该正确");

        // 支付和状态信息校验
        assertEquals(PaymentChannelEnum.OFFLINE.name(), capturedOrder.getPaymentChannel(), "支付渠道应该正确");
        assertEquals(OrderStatusEnum.NOT_PAY.name(), capturedOrder.getOrderStatus(), "订单状态应该为未支付");
        assertNotNull(capturedOrder.getOrderCreateTime(), "订单创建时间不应为null");

        // 验证保存的订单详情数据 - 详细字段校验
        ArgumentCaptor<OrderDetailEnergyTopUpEntity> detailCaptor = ArgumentCaptor.forClass(OrderDetailEnergyTopUpEntity.class);
        verify(orderDetailEnergyTopUpRepository).insert(detailCaptor.capture());
        OrderDetailEnergyTopUpEntity capturedDetail = detailCaptor.getValue();

        // 订单详情基本信息校验
        assertEquals(capturedOrder.getOrderSn(), capturedDetail.getOrderSn(), "订单详情的订单号应该与主订单一致");
        assertEquals(1, capturedDetail.getOwnerId(), "所有者ID应该正确");
        assertEquals(OwnerTypeEnum.ENTERPRISE.getCode(), capturedDetail.getOwnerType(), "所有者类型应该正确");
        assertEquals("测试用户", capturedDetail.getOwnerName(), "所有者名称应该正确");
        assertEquals(1, capturedDetail.getAccountId(), "账户ID应该正确");

        // 电表相关信息校验
        assertEquals(100, capturedDetail.getMeterId(), "电表ID应该正确");
        assertEquals("测试电表", capturedDetail.getMeterName(), "电表名称应该正确");
        assertEquals("METER001", capturedDetail.getDeviceNo(), "电表编号应该正确");

        // 空间相关信息校验
        assertEquals(1, capturedDetail.getSpaceId(), "空间ID应该正确");
        assertEquals("测试空间", capturedDetail.getSpaceName(), "空间名称应该正确");
        assertEquals("0,1", capturedDetail.getSpaceParentIds(), "空间父级ID应该正确");
        assertEquals("根节点,测试空间", capturedDetail.getSpaceParentNames(), "空间父级名称应该正确");
        assertNotNull(capturedDetail.getCreateTime(), "订单详情创建时间不应为null");
        assertEquals(capturedOrder.getOrderCreateTime(), capturedDetail.getCreateTime(), "订单详情创建时间应该与订单创建时间一致");
    }

    @Test
    void testHandle_WhenEnterpriseUserCreateOrder_ShouldSetRegionId() {
        // Given
        // 模拟依赖服务的行为
        when(spaceService.getDetail(anyInt())).thenReturn(spaceBo);
        when(orderRepository.insert(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            entity.setOrderSn("SN_TEST");
            return 1;
        });
        when(orderDetailEnergyTopUpRepository.insert(any(OrderDetailEnergyTopUpEntity.class))).thenReturn(1);
        when(serviceRateService.getDefaultServiceRate()).thenReturn(new BigDecimal("1.00"));
        when(orderMapper.toBo(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            return new OrderBo()
                    .setOrderSn(entity.getOrderSn())
                    .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                    .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                    .setOrderStatus(OrderStatusEnum.NOT_PAY)
                    .setOrderAmount(entity.getOrderAmount());
        });

        // When
        OrderBo result = handler.createOrder(energyTopUpDto);

        // Then
        assertNotNull(result, "返回的订单不应为null");

        // 验证保存的订单数据 - 企业用户特殊字段校验
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).insert(orderCaptor.capture());
        OrderEntity capturedOrder = orderCaptor.getValue();

        // 验证订单详情数据保存
        ArgumentCaptor<OrderDetailEnergyTopUpEntity> detailCaptor = ArgumentCaptor.forClass(OrderDetailEnergyTopUpEntity.class);
        verify(orderDetailEnergyTopUpRepository).insert(detailCaptor.capture());
        OrderDetailEnergyTopUpEntity capturedDetail = detailCaptor.getValue();

        // 验证企业用户相关字段
        assertEquals(OwnerTypeEnum.ENTERPRISE.getCode(), capturedDetail.getOwnerType(), "应该是企业类型用户");
        assertEquals(1, capturedDetail.getOwnerId(), "企业ID应该正确");
        assertEquals("测试用户", capturedDetail.getOwnerName(), "企业名称应该正确");
        assertNotNull(capturedDetail.getCreateTime(), "订单详情创建时间不应为null");
        assertEquals(capturedOrder.getOrderCreateTime(), capturedDetail.getCreateTime(), "订单详情创建时间应该与订单创建时间一致");

        // 验证方法调用次数
        verify(spaceService).getDetail(1);
        verify(orderRepository).insert(any(OrderEntity.class));
        verify(orderDetailEnergyTopUpRepository).insert(any(OrderDetailEnergyTopUpEntity.class));
    }

    @Test
    void testHandle_WhenSaveOrderFails_ShouldThrowException() {
        // Given
        when(orderRepository.insert(any(OrderEntity.class))).thenThrow(new BusinessRuntimeException("订单保存失败")); // 模拟保存失败
        when(serviceRateService.getDefaultServiceRate()).thenReturn(new BigDecimal("1.00"));

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            handler.createOrder(energyTopUpDto);
        });

        assertEquals("订单保存失败", exception.getMessage());

        // 验证方法调用情况 - 订单保存失败时不应该保存订单详情
        verify(orderRepository).insert(any(OrderEntity.class));
        verify(orderDetailEnergyTopUpRepository, never()).insert(any(OrderDetailEnergyTopUpEntity.class));
    }
}
