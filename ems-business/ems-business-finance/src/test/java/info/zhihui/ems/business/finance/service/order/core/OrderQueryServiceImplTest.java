package info.zhihui.ems.business.finance.service.order.core;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.qo.OrderQueryQo;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.impl.OrderQueryServiceImpl;
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

    @InjectMocks
    private OrderQueryServiceImpl orderQueryService;

    private OrderQueryDto queryDto;
    private PageParam pageParam;
    private OrderEntity orderEntity1;
    private OrderEntity orderEntity2;
    private OrderBo orderBo1;
    private OrderBo orderBo2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setCreateStartTime(sevenDaysAgo)
                .setCreateEndTime(now)
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setUserId(123);
        pageParam = new PageParam()
                .setPageNum(1)
                .setPageSize(10);

        orderEntity1 = new OrderEntity();
        orderEntity1.setOrderSn("ORDER001");
        orderEntity1.setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        orderEntity1.setOrderCreateTime(now.minusDays(3));

        orderEntity2 = new OrderEntity();
        orderEntity2.setOrderSn("ORDER002");
        orderEntity2.setOrderStatus(OrderStatusEnum.NOT_PAY.name());
        orderEntity2.setOrderCreateTime(now.minusDays(5));

        orderBo1 = new OrderBo();
        orderBo1.setOrderSn("ORDER001");
        orderBo1.setOrderStatus(OrderStatusEnum.NOT_PAY);
        orderBo1.setOrderCreateTime(now.minusDays(3));

        orderBo2 = new OrderBo();
        orderBo2.setOrderSn("ORDER002");
        orderBo2.setOrderStatus(OrderStatusEnum.NOT_PAY);
        orderBo2.setOrderCreateTime(now.minusDays(5));
    }

    @Test
    void testFindOrdersPage_Success() {
        List<OrderEntity> orderEntities = Arrays.asList(orderEntity1, orderEntity2);
        PageResult<OrderBo> pageResult = new PageResult<OrderBo>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(2L)
                .setList(List.of(orderBo1, orderBo2));
        when(orderRepository.findList(any(OrderQueryQo.class))).thenReturn(orderEntities);
        when(orderMapper.pageEntityToBo(any())).thenReturn(pageResult);

        PageResult<OrderBo> result = orderQueryService.findOrdersPage(queryDto, pageParam);

        assertNotNull(result);
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(2L, result.getTotal());
        assertNotNull(result.getList());
        assertEquals(2, result.getList().size());
        verify(orderRepository).findList(argThat(qo ->
                OrderStatusEnum.NOT_PAY.name().equals(qo.getOrderStatus())
                        && PaymentChannelEnum.WX_MINI.name().equals(qo.getPaymentChannel())
                        && Integer.valueOf(123).equals(qo.getUserId())
                        && queryDto.getCreateStartTime().equals(qo.getCreateStartTime())
                        && queryDto.getCreateEndTime().equals(qo.getCreateEndTime())));
        verify(orderMapper).pageEntityToBo(any());
    }

    @Test
    void testFindOrdersPage_RepositoryException() {
        when(orderRepository.findList(any(OrderQueryQo.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderQueryService.findOrdersPage(queryDto, pageParam));

        assertEquals("Database error", exception.getMessage());
        verify(orderRepository).findList(any(OrderQueryQo.class));
        verify(orderMapper, never()).pageEntityToBo(any());
    }

    @Test
    void testFindOrdersPage_PageMapperException() {
        when(orderRepository.findList(any(OrderQueryQo.class))).thenReturn(List.of(orderEntity1));
        when(orderMapper.pageEntityToBo(any())).thenThrow(new RuntimeException("Mapping error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderQueryService.findOrdersPage(queryDto, pageParam));

        assertEquals("Mapping error", exception.getMessage());
        verify(orderRepository).findList(any(OrderQueryQo.class));
        verify(orderMapper).pageEntityToBo(any());
    }
}
