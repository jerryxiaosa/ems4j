package info.zhihui.ems.business.finance.service.order.handler.creation;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationOrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationSettlementDto;
import info.zhihui.ems.business.finance.entity.order.OrderDetailTerminationEntity;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderDetailTerminationRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.handler.impl.OrderTerminationHandler;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OrderTerminationHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailTerminationRepository orderDetailTerminationRepository;
    @Mock
    private OrderMapper orderMapper;

    private OrderTerminationHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new OrderTerminationHandler(orderMapper, orderRepository, orderDetailTerminationRepository);
    }

    @Test
    void createOrder_ShouldCreateOrderAndDetail() {
        TerminationOrderCreationInfoDto dto = buildCreationDto(BigDecimal.valueOf(100));

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        ArgumentCaptor<OrderDetailTerminationEntity> detailCaptor = ArgumentCaptor.forClass(OrderDetailTerminationEntity.class);

        given(orderRepository.insert(any(OrderEntity.class))).willAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            entity.setOrderSn("SN123456");
            return 1;
        });
        given(orderDetailTerminationRepository.insert(any(OrderDetailTerminationEntity.class))).willReturn(1);
        given(orderMapper.toBo(any(OrderEntity.class))).willAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            return new OrderBo()
                    .setOrderSn(entity.getOrderSn())
                    .setOrderType(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT)
                    .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                    .setThirdPartyUserId(entity.getThirdPartyUserId())
                    .setOrderAmount(entity.getOrderAmount());
        });

        OrderBo result = handler.createOrder(dto);

        verify(orderRepository, times(1)).insert(orderCaptor.capture());
        verify(orderDetailTerminationRepository, times(1)).insert(detailCaptor.capture());

        OrderEntity savedOrder = orderCaptor.getValue();
        assertEquals(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT.getCode(), savedOrder.getOrderType());
        assertEquals(0, BigDecimal.ZERO.compareTo(savedOrder.getServiceAmount()));
        assertEquals(OrderStatusEnum.NOT_PAY.name(), savedOrder.getOrderStatus());
        assertEquals("wx-open-id", savedOrder.getThirdPartyUserId());

        OrderDetailTerminationEntity savedDetail = detailCaptor.getValue();
        assertEquals("CANCEL-001", savedDetail.getCancelNo());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(savedDetail.getSettlementAmount()));

        assertNotNull(result);
        assertEquals("SN123456", result.getOrderSn());
        assertEquals(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT, result.getOrderType());
        assertEquals(PaymentChannelEnum.OFFLINE, result.getPaymentChannel());
        assertEquals("wx-open-id", result.getThirdPartyUserId());
    }

    @Test
    void createOrder_ShouldThrowWhenSettlementAmountMismatch() {
        TerminationOrderCreationInfoDto dto = buildCreationDto(BigDecimal.valueOf(100));
        dto.setOrderAmount(BigDecimal.valueOf(200));

        assertThrows(BusinessRuntimeException.class, () -> handler.createOrder(dto));
    }

    @Test
    void createOrder_ShouldThrowWhenDtoNull() {
        assertThrows(BusinessRuntimeException.class, () -> handler.createOrder(null));
    }

    private TerminationOrderCreationInfoDto buildCreationDto(BigDecimal amount) {
        TerminationSettlementDto settlementDto = new TerminationSettlementDto()
                .setCancelNo("CANCEL-001")
                .setAccountId(1)
                .setOwnerId(10)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setSettlementAmount(amount)
                .setCloseReason("测试销户");

        TerminationOrderCreationInfoDto infoDto = new TerminationOrderCreationInfoDto();
        infoDto.setUserId(100);
        infoDto.setUserPhone("18800001111");
        infoDto.setUserRealName("张三");
        infoDto.setThirdPartyUserId("wx-open-id");
        infoDto.setOrderAmount(amount);
        infoDto.setPaymentChannel(PaymentChannelEnum.OFFLINE);
        infoDto.setTerminationInfo(settlementDto);
        return infoDto;
    }
}
