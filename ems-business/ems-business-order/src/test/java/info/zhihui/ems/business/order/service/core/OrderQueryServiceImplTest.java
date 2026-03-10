package info.zhihui.ems.business.order.service.core;

import info.zhihui.ems.business.order.dto.OrderListDto;
import info.zhihui.ems.business.order.dto.OrderQueryDto;
import info.zhihui.ems.business.order.enums.OrderStatusEnum;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.order.enums.PaymentChannelEnum;
import info.zhihui.ems.business.order.mapstruct.OrderMapper;
import info.zhihui.ems.business.order.qo.OrderListItemQo;
import info.zhihui.ems.business.order.qo.OrderQueryQo;
import info.zhihui.ems.business.order.repository.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.order.repository.OrderRepository;
import info.zhihui.ems.business.order.service.core.impl.OrderQueryServiceImpl;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderQueryServiceImpl 单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class OrderQueryServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;

    @InjectMocks
    private OrderQueryServiceImpl orderQueryService;

    private OrderQueryDto queryDto;
    private PageParam pageParam;
    private OrderListItemQo orderItemQo1;
    private OrderListItemQo orderItemQo2;
    private OrderListDto orderBo1;
    private OrderListDto orderBo2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        queryDto = new OrderQueryDto()
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setOrderSnLike(" ORDER ")
                .setThirdPartySnLike(" TP ")
                .setEnterpriseNameLike(" ACME CORP ")
                .setCreateStartTime(sevenDaysAgo)
                .setCreateEndTime(now)
                .setPaymentChannel(PaymentChannelEnum.WX_MINI);
        pageParam = new PageParam()
                .setPageNum(1)
                .setPageSize(10);

        orderItemQo1 = new OrderListItemQo();
        orderItemQo1.setOrderSn("ORDER001");
        orderItemQo1.setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        orderItemQo1.setOrderCreateTime(now.minusDays(3));

        orderItemQo2 = new OrderListItemQo();
        orderItemQo2.setOrderSn("ORDER002");
        orderItemQo2.setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        orderItemQo2.setOrderCreateTime(now.minusDays(5));

        orderBo1 = new OrderListDto();
        orderBo1.setOrderSn("ORDER001");
        orderBo1.setOrderStatus(OrderStatusEnum.NOT_PAY);
        orderBo1.setOrderCreateTime(now.minusDays(3));

        orderBo2 = new OrderListDto();
        orderBo2.setOrderSn("ORDER002");
        orderBo2.setOrderStatus(OrderStatusEnum.NOT_PAY);
        orderBo2.setOrderCreateTime(now.minusDays(5));
    }

    @Test
    void testFindOrdersPage_Success() {
        List<OrderListItemQo> orderItemQos = Arrays.asList(orderItemQo1, orderItemQo2);
        PageResult<OrderListDto> pageResult = new PageResult<OrderListDto>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(2L)
                .setList(List.of(orderBo1, orderBo2));
        when(orderRepository.findList(any(OrderQueryQo.class))).thenReturn(orderItemQos);
        when(orderMapper.pageOrderListItemQoToOrderListDto(any())).thenReturn(pageResult);

        PageResult<OrderListDto> result = orderQueryService.findOrdersPage(queryDto, pageParam);

        assertNotNull(result);
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(2L, result.getTotal());
        assertNotNull(result.getList());
        assertEquals(2, result.getList().size());
        verify(orderRepository).findList(argThat(qo ->
                OrderTypeEnum.ENERGY_TOP_UP.getCode().equals(qo.getOrderType())
                        && OrderStatusEnum.NOT_PAY.name().equals(qo.getOrderStatus())
                        && "ORDER".equals(qo.getOrderSnLike())
                        && "TP".equals(qo.getThirdPartySnLike())
                        && "ACME CORP".equals(qo.getEnterpriseNameLike())
                        && OwnerTypeEnum.ENTERPRISE.getCode().equals(qo.getOwnerType())
                        && PaymentChannelEnum.WX_MINI.name().equals(qo.getPaymentChannel())
                        && queryDto.getCreateStartTime().equals(qo.getCreateStartTime())
                        && queryDto.getCreateEndTime().equals(qo.getCreateEndTime())));
        verify(orderMapper).pageOrderListItemQoToOrderListDto(any());
    }

    @Test
    void testFindOrdersPage_RepositoryException() {
        when(orderRepository.findList(any(OrderQueryQo.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderQueryService.findOrdersPage(queryDto, pageParam));

        assertEquals("Database error", exception.getMessage());
        verify(orderRepository).findList(any(OrderQueryQo.class));
        verify(orderMapper, never()).pageOrderListItemQoToOrderListDto(any());
    }

    @Test
    void testFindOrdersPage_PageMapperException() {
        when(orderRepository.findList(any(OrderQueryQo.class))).thenReturn(List.of(orderItemQo1));
        when(orderMapper.pageOrderListItemQoToOrderListDto(any())).thenThrow(new RuntimeException("Mapping error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderQueryService.findOrdersPage(queryDto, pageParam));

        assertEquals("Mapping error", exception.getMessage());
        verify(orderRepository).findList(any(OrderQueryQo.class));
        verify(orderMapper).pageOrderListItemQoToOrderListDto(any());
    }

    @Test
    void testFindOrdersPage_WhenNoEnergyTopUpOrder_ShouldNotQueryTopUpDetail() {
        OrderListDto settlementOrder = new OrderListDto()
                .setOrderSn("ORDER003")
                .setOrderType(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT);
        PageResult<OrderListDto> pageResult = new PageResult<OrderListDto>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L)
                .setList(List.of(settlementOrder));
        when(orderRepository.findList(any(OrderQueryQo.class))).thenReturn(List.of(orderItemQo1));
        when(orderMapper.pageOrderListItemQoToOrderListDto(any())).thenReturn(pageResult);

        orderQueryService.findOrdersPage(queryDto, pageParam);

        verify(orderDetailEnergyTopUpRepository, never()).findByOrderSnList(any());
    }

    @Test
    void testFindOrdersPage_WhenMixedOrderType_ShouldOnlyQueryEnergyTopUpOrderSn() {
        OrderListDto topUpOrder = new OrderListDto()
                .setOrderSn("ORDER001")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP);
        OrderListDto settlementOrder = new OrderListDto()
                .setOrderSn("ORDER002")
                .setOrderType(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT);
        PageResult<OrderListDto> pageResult = new PageResult<OrderListDto>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(2L)
                .setList(List.of(topUpOrder, settlementOrder));
        when(orderRepository.findList(any(OrderQueryQo.class))).thenReturn(List.of(orderItemQo1, orderItemQo2));
        when(orderMapper.pageOrderListItemQoToOrderListDto(any())).thenReturn(pageResult);

        orderQueryService.findOrdersPage(queryDto, pageParam);

        verify(orderDetailEnergyTopUpRepository).findByOrderSnList(argThat((List<String> orderSnList) ->
                orderSnList != null
                        && orderSnList.size() == 1
                        && orderSnList.contains("ORDER001")));
    }
}
