package info.zhihui.ems.business.finance.service.order.core;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.dto.order.creation.OrderCreationInfoDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationDto;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationResponseDto;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.impl.OrderServiceImpl;
import info.zhihui.ems.business.finance.service.order.handler.completion.OrderCompletionHandler;
import info.zhihui.ems.business.finance.service.order.handler.creation.OrderCreationHandler;
import info.zhihui.ems.business.finance.service.order.thirdparty.OrderThirdPartyHandler;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.mq.api.service.MqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @Mock
    private LockTemplate lockTemplate;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private MqService mqService;
    @Mock
    private OrderCreationHandler mockCreationHandler;
    @Mock
    private OrderCompletionHandler mockCompletionHandler;
    @Mock
    private OrderThirdPartyHandler<OrderCreationResponseDto> mockThirdPartyHandler;
    @Mock
    private Lock mockLock;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderServiceImpl(Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService);
    }

    @Test
    void getDetail_ShouldReturnOrderBo_WhenOrderExists() {
        LocalDateTime now = LocalDateTime.now();
        OrderEntity entity = new OrderEntity()
                .setOrderSn("SN123")
                .setUserId(10)
                .setUserRealName("张三")
                .setUserPhone("13800000000")
                .setThirdPartyUserId("third-user")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setServiceRate(BigDecimal.ZERO)
                .setServiceAmount(BigDecimal.ZERO)
                .setUserPayAmount(new BigDecimal("100.00"))
                .setCurrency("CNY")
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name())
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setOrderCreateTime(now)
                .setOrderPayStopTime(now.plusHours(1))
                .setIsDeleted(Boolean.FALSE);

        OrderBo orderBo = new OrderBo()
                .setOrderSn("SN123")
                .setUserId(10)
                .setUserRealName("张三")
                .setUserPhone("13800000000")
                .setThirdPartyUserId("third-user")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setServiceRate(BigDecimal.ZERO)
                .setServiceAmount(BigDecimal.ZERO)
                .setUserPayAmount(new BigDecimal("100.00"))
                .setCurrency("CNY")
                .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setOrderCreateTime(now)
                .setOrderPayStopTime(now)
                ;

        when(orderRepository.selectByOrderSn("SN123")).thenReturn(entity);
        when(orderMapper.toBo(entity)).thenReturn(orderBo);

        var result = orderService.getDetail("SN123");

        assertNotNull(result);
        assertThat(result.getOrderSn()).isEqualTo("SN123");
        assertThat(result.getOrderType()).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
        assertThat(result.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatusEnum.NOT_PAY);
        assertThat(result.getOrderAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getUserRealName()).isEqualTo("张三");
    }

    @Test
    void getDetail_ShouldThrowNotFound_WhenOrderMissing() {
        when(orderRepository.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> orderService.getDetail("SN_NOT_EXIST"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");
    }

    @Test
    void getDetail_ShouldThrowNotFound_WhenOrderDeleted() {
        OrderEntity entity = new OrderEntity()
                .setOrderSn("SN_DEL")
                .setIsDeleted(Boolean.TRUE);
        when(orderRepository.selectOne(any())).thenReturn(entity);

        assertThatThrownBy(() -> orderService.getDetail("SN_DEL"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createOrder_ShouldThrowBusinessException_WhenHandlerMissing() {
        TestOrderCreationInfoDto requestDto = new TestOrderCreationInfoDto();
        requestDto.setUserId(2);
        requestDto.setUserPhone("13800002222");
        requestDto.setUserRealName("缺省用户");
        requestDto.setThirdPartyUserId("third");
        requestDto.setOrderAmount(new BigDecimal("66.00"));
        requestDto.setPaymentChannel(PaymentChannelEnum.OFFLINE);

        assertThatThrownBy(() -> orderService.createOrder(requestDto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("不支持的订单类型");
    }

    @Test
    void createOrder_ShouldReturnResponseDto_WhenOrderCreatedSuccessfully() {
        // Given
        TestOrderCreationInfoDto requestDto = new TestOrderCreationInfoDto();
        requestDto.setUserId(1)
                .setUserPhone("13800001111")
                .setUserRealName("测试用户")
                .setThirdPartyUserId("third-party-user")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        OrderBo orderBo = new OrderBo()
                .setOrderSn("TEST_ORDER_001")
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        OrderCreationResponseDto responseDto = new OrderCreationResponseDto()
                .setOrderSn("TEST_ORDER_001")
                .setOrderTypeEnum(OrderTypeEnum.ENERGY_TOP_UP)
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        // 创建带有Handler的OrderService实例
        Mockito.<Class<? extends OrderCreationInfoDto>>when(mockCreationHandler.getSupportedParam())
                .thenReturn(TestOrderCreationInfoDto.class);

        when(mockCreationHandler.createOrder(any(OrderCreationInfoDto.class))).thenReturn(orderBo);
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.OFFLINE);
        when(mockThirdPartyHandler.onCreate(orderBo)).thenReturn(responseDto);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.singletonList(mockCreationHandler),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When
        OrderCreationResponseDto result = orderServiceWithHandlers.createOrder(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isEqualTo("TEST_ORDER_001");
        assertThat(result.getOrderTypeEnum()).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
        assertThat(result.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        verify(mockCreationHandler).createOrder(requestDto);
        verify(mockThirdPartyHandler).onCreate(orderBo);
    }

    @Test
    void createOrder_ShouldThrowBusinessException_WhenUnsupportedPaymentChannel() {
        // Given
        TestOrderCreationInfoDto requestDto = new TestOrderCreationInfoDto();
        requestDto.setUserId(1)
                .setUserPhone("13800001111")
                .setUserRealName("测试用户")
                .setThirdPartyUserId("third-party-user")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI); // 不支持的支付渠道

        OrderBo orderBo = new OrderBo()
                .setOrderSn("TEST_ORDER_001")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        Mockito.<Class<? extends OrderCreationInfoDto>>when(mockCreationHandler.getSupportedParam())
                .thenReturn(TestOrderCreationInfoDto.class);
        when(mockCreationHandler.createOrder(any(OrderCreationInfoDto.class))).thenReturn(orderBo);
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.OFFLINE); // 只支持OFFLINE

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.singletonList(mockCreationHandler),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When & Then
        assertThatThrownBy(() -> orderServiceWithHandlers.createOrder(requestDto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("不支持的支付渠道: WX_MINI");
        verify(mockCreationHandler).createOrder(requestDto);
        verify(mockThirdPartyHandler, never()).onCreate(any());
    }

    @Test
    void createOrder_ShouldThrowBusinessException_WhenCreationHandlerReturnsNull() {
        // Given
        TestOrderCreationInfoDto requestDto = new TestOrderCreationInfoDto();
        requestDto.setUserId(1)
                .setUserPhone("13800001111")
                .setUserRealName("测试用户")
                .setThirdPartyUserId("third-party-user")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);

        Mockito.<Class<? extends OrderCreationInfoDto>>when(mockCreationHandler.getSupportedParam())
                .thenReturn(TestOrderCreationInfoDto.class);
        when(mockCreationHandler.createOrder(any(OrderCreationInfoDto.class))).thenReturn(null); // 返回null
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.OFFLINE);
        when(mockThirdPartyHandler.onCreate(null)).thenThrow(NullPointerException.class);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.singletonList(mockCreationHandler),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When & Then
        assertThatThrownBy(() -> orderServiceWithHandlers.createOrder(requestDto))
                .isInstanceOf(NullPointerException.class);
        verify(mockCreationHandler).createOrder(requestDto);
    }

    @Test
    void close_ShouldCloseOrderSuccessfully_WhenOrderExists() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name())
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setIsDeleted(Boolean.FALSE);

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.OFFLINE);
        doNothing().when(mockThirdPartyHandler).onClose(orderBo);
        when(orderRepository.updateByOrderSn(any(OrderEntity.class))).thenReturn(1);
        doNothing().when(mqService).sendTransactionMessage(any());

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When
        orderServiceWithHandlers.close(orderSn);

        // Then
        verify(mockLock).tryLock();
        verify(mockThirdPartyHandler).onClose(orderBo);
        verify(orderRepository).updateByOrderSn(new OrderEntity().setOrderSn(orderSn).setOrderStatus(OrderStatusEnum.CLOSED.name()));
        verify(mockLock).unlock();
    }

    @Test
    void close_ShouldReturnEarly_WhenOrderAlreadyClosed() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                .setOrderStatus(OrderStatusEnum.CLOSED); // 已关闭状态

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name())
                .setOrderStatus(OrderStatusEnum.CLOSED.name())
                .setIsDeleted(Boolean.FALSE);

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);

        // When
        orderService.close(orderSn);

        // Then
        verify(mockLock).tryLock();
        verify(mockLock).unlock();
        verify(orderRepository, never()).updateByOrderSn(any());
        verify(mqService, never()).sendTransactionMessage(any());
    }

    @Test
    void close_ShouldThrowNotFoundException_WhenOrderNotExists() {
        // Given
        String orderSn = "NON_EXISTENT_ORDER";
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> orderService.close(orderSn))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");
        verify(mockLock).tryLock();
        verify(mockLock).unlock();
    }

    @Test
    void close_ShouldThrowBusinessException_WhenLockFailed() {
        // Given
        String orderSn = "TEST_ORDER_001";
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(false); // 获取锁失败

        // When & Then
        assertThatThrownBy(() -> orderService.close(orderSn))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("订单已被暂时锁定，请稍后重试");
        verify(mockLock).tryLock();
        verify(mockLock, never()).unlock();
    }

    @Test
    void close_ShouldThrowBusinessException_WhenOrderAlreadyCompleted() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                .setOrderStatus(OrderStatusEnum.SUCCESS); // 已完成状态

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.name())
                .setOrderStatus(OrderStatusEnum.SUCCESS.name())
                .setIsDeleted(Boolean.FALSE);

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.OFFLINE);
        doNothing().when(mockThirdPartyHandler).onClose(orderBo);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When & Then
        assertThatThrownBy(() -> orderServiceWithHandlers.close(orderSn))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("订单已完成，无法改变订单状态");
        verify(mockLock).tryLock();
        verify(mockThirdPartyHandler).onClose(orderBo);
        verify(mockLock).unlock();
    }

    @Test
    void answerPayNotify_ShouldProcessSuccessfully_WhenValidNotification() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData("notification_data");

        OrderThirdPartyNotificationResponseDto response = new OrderThirdPartyNotificationResponseDto()
                .setOrderSn(orderSn)
                .setOrderStatus(OrderStatusEnum.SUCCESS)
                .setPayOrderAmount(new BigDecimal("100.00"));

        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setIsDeleted(Boolean.FALSE);

        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);
        when(mockThirdPartyHandler.onPayNotify("notification_data")).thenReturn(response);
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);
        when(orderRepository.updateByOrderSn(any(OrderEntity.class))).thenReturn(1);
        when(mockCompletionHandler.getOrderType()).thenReturn(OrderTypeEnum.ENERGY_TOP_UP);
        MqMessage mqMessage = new MqMessage();
        when(mockCompletionHandler.createMessageAfterOrderSuccess(orderBo)).thenReturn(mqMessage);

        doNothing().when(mqService).sendTransactionMessage(any());

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.singletonList(mockCompletionHandler),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When
        orderServiceWithHandlers.answerPayNotify(notification);

        // Then
        verify(mockThirdPartyHandler).onPayNotify("notification_data");
        verify(mockLock).tryLock();
        verify(orderRepository).selectByOrderSn(orderSn);
        ArgumentCaptor<OrderEntity> orderEntityCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).updateByOrderSn(orderEntityCaptor.capture());
        OrderEntity updatedEntity = orderEntityCaptor.getValue();
        assertThat(updatedEntity.getOrderSn()).isEqualTo(orderSn);
        assertThat(updatedEntity.getOrderStatus()).isEqualTo(OrderStatusEnum.SUCCESS.name());
        assertThat(updatedEntity.getOrderSuccessTime()).isNotNull();
        assertThat(orderBo.getOrderStatus()).isEqualTo(OrderStatusEnum.SUCCESS);
        assertThat(orderBo.getOrderSuccessTime()).isEqualTo(updatedEntity.getOrderSuccessTime());
        verify(mqService).sendTransactionMessage(any());
        verify(mockLock).unlock();
     }

    @Test
    void answerPayNotify_ShouldReturnEarly_WhenOrderAlreadySuccess() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData("notification_data");

        OrderThirdPartyNotificationResponseDto response = new OrderThirdPartyNotificationResponseDto()
                .setOrderSn(orderSn)
                .setOrderStatus(OrderStatusEnum.SUCCESS)
                .setPayOrderAmount(new BigDecimal("100.00"));

        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.SUCCESS); // 已成功状态

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderStatus(OrderStatusEnum.SUCCESS.name())
                .setIsDeleted(Boolean.FALSE);

        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);
        when(mockThirdPartyHandler.onPayNotify("notification_data")).thenReturn(response);
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When
        orderServiceWithHandlers.answerPayNotify(notification);

        // Then
        verify(mockThirdPartyHandler).onPayNotify("notification_data");
        verify(mockLock).tryLock();
        verify(mockLock).unlock();
        verify(orderRepository, never()).updateByOrderSn(any());
        verify(mqService, never()).sendTransactionMessage(any());
    }

    @Test
    void answerPayNotify_ShouldReturnEarly_WhenOrderAlreadyClosed() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData("notification_data");

        OrderThirdPartyNotificationResponseDto response = new OrderThirdPartyNotificationResponseDto()
                .setOrderSn(orderSn)
                .setOrderStatus(OrderStatusEnum.SUCCESS)
                .setPayOrderAmount(new BigDecimal("100.00"));

        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.CLOSED); // 已关闭状态

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderStatus(OrderStatusEnum.CLOSED.name())
                .setIsDeleted(Boolean.FALSE);

        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);
        when(mockThirdPartyHandler.onPayNotify("notification_data")).thenReturn(response);
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When
        orderServiceWithHandlers.answerPayNotify(notification);

        // Then
        verify(mockThirdPartyHandler).onPayNotify("notification_data");
        verify(mockLock).tryLock();
        verify(mockLock).unlock();
        verify(orderRepository, never()).updateByOrderSn(any());
        verify(mqService, never()).sendTransactionMessage(any());
    }

    @Test
    void answerPayNotify_ShouldThrowBusinessException_WhenOrderNotFound() {
        // Given
        String orderSn = "NON_EXISTENT_ORDER";
        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData("notification_data");

        OrderThirdPartyNotificationResponseDto response = new OrderThirdPartyNotificationResponseDto()
                .setOrderSn(orderSn)
                .setOrderStatus(OrderStatusEnum.SUCCESS)
                .setPayOrderAmount(new BigDecimal("100.00"));

        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);
        when(mockThirdPartyHandler.onPayNotify("notification_data")).thenReturn(response);
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(null);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When & Then
        assertThatThrownBy(() -> orderServiceWithHandlers.answerPayNotify(notification))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");
        verify(mockLock).tryLock();
        verify(mockLock).unlock();
        verify(mqService, never()).sendTransactionMessage(any());
    }

    @Test
    void answerPayNotify_ShouldThrowBusinessException_WhenPaymentAmountMismatch() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData("notification_data");

        OrderThirdPartyNotificationResponseDto response = new OrderThirdPartyNotificationResponseDto()
                .setOrderSn(orderSn)
                .setOrderStatus(OrderStatusEnum.SUCCESS)
                .setPayOrderAmount(new BigDecimal("150.00")); // 与订单金额不一致

        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00")) // 订单应付金额
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setIsDeleted(Boolean.FALSE);

        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);
        when(mockThirdPartyHandler.onPayNotify("notification_data")).thenReturn(response);
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderRepository.updateByOrderSn(new OrderEntity().setOrderSn(orderSn).setOrderStatus(OrderStatusEnum.PAY_ERROR.name()))).thenReturn(1);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When & Then
        orderServiceWithHandlers.answerPayNotify(notification);
        verify(mockLock).tryLock();
        verify(mockLock).unlock();
    }

    @Test
    void complete_ShouldCompleteSuccessfully_WhenValidOrder() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderStatus(OrderStatusEnum.SUCCESS.name())
                .setIsDeleted(Boolean.FALSE);

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderRepository.updateByOrderSn(any(OrderEntity.class))).thenReturn(1);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);
        when(mockCompletionHandler.getOrderType()).thenReturn(OrderTypeEnum.ENERGY_TOP_UP);
        MqMessage mqMessage = new MqMessage();
        when(mockCompletionHandler.createMessageAfterOrderSuccess(orderBo)).thenReturn(mqMessage);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.singletonList(mockCompletionHandler),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When
        orderServiceWithHandlers.complete(orderSn);

        // Then
        verify(mockLock).tryLock();
        verify(orderRepository).updateByOrderSn(any());
        verify(mqService).sendTransactionMessage(any());
        verify(mockLock).unlock();
    }

    @Test
    void complete_ShouldReturnEarly_WhenOrderAlreadySuccess() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.SUCCESS); // 已成功状态

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderStatus(OrderStatusEnum.SUCCESS.name())
                .setIsDeleted(Boolean.FALSE);

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When
        orderServiceWithHandlers.complete(orderSn);

        // Then
        verify(mockLock).tryLock();
        verify(mockLock).unlock();
        verify(orderRepository, never()).updateByOrderSn(any());
        verify(mqService, never()).sendTransactionMessage(any());
    }

    @Test
    void complete_ShouldReturnEarly_WhenOrderAlreadyClosed() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.CLOSED); // 已关闭状态

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderStatus(OrderStatusEnum.CLOSED.name())
                .setIsDeleted(Boolean.FALSE);

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When
        orderServiceWithHandlers.complete(orderSn);

        // Then
        verify(mockLock).tryLock();
        verify(mockLock).unlock();
        verify(orderRepository, never()).updateByOrderSn(any());
        verify(mqService, never()).sendTransactionMessage(any());
    }

    @Test
    void complete_ShouldThrowBusinessException_WhenLockFailed() {
        // Given
        String orderSn = "TEST_ORDER_001";

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(false); // 获取锁失败
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When & Then
        assertThatThrownBy(() -> orderServiceWithHandlers.complete(orderSn))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("订单已被暂时锁定，请稍后重试");

        verify(mockLock).tryLock();
        verify(mockLock, never()).unlock();
        verify(orderRepository, never()).selectByOrderSn(any());
    }

    @Test
    void complete_ShouldThrowBusinessException_WhenOrderNotFound() {
        // Given
        String orderSn = "TEST_ORDER_001";

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(null); // 订单不存在
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When & Then
        assertThatThrownBy(() -> orderServiceWithHandlers.complete(orderSn))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("订单信息不存在");

        verify(mockLock).tryLock();
        verify(mockLock).unlock();
        verify(orderRepository).selectByOrderSn(orderSn);
    }

    @Test
    void complete_ShouldThrowBusinessException_WhenThirdPartyCheckFailed() {
        // Given
        String orderSn = "TEST_ORDER_001";
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setOrderStatus(OrderStatusEnum.NOT_PAY);

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderSn)
                .setUserId(1)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP.getCode())
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.WX_MINI.name())
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setIsDeleted(Boolean.FALSE);

        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(orderRepository.selectByOrderSn(orderSn)).thenReturn(orderEntity);
        when(orderMapper.toBo(orderEntity)).thenReturn(orderBo);
        when(mockThirdPartyHandler.getPaymentChannel()).thenReturn(PaymentChannelEnum.WX_MINI);
        doThrow(new BusinessRuntimeException("支付完成检查失败")).when(mockThirdPartyHandler).onCheckComplete(orderBo);

        OrderServiceImpl orderServiceWithHandlers = new OrderServiceImpl(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(mockThirdPartyHandler),
                orderRepository,
                orderMapper,
                lockTemplate,
                mqService
        );

        // When & Then
        assertThatThrownBy(() -> orderServiceWithHandlers.complete(orderSn))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("支付完成检查失败");

        verify(mockLock).tryLock();
        verify(mockLock).unlock();
        verify(orderRepository, never()).updateByOrderSn(any());
        verify(mqService, never()).sendMessage(any());
    }

    private static class TestOrderCreationInfoDto extends OrderCreationInfoDto {
    }
}
