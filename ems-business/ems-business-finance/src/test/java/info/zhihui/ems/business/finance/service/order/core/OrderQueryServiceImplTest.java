package info.zhihui.ems.business.finance.service.order.core;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.impl.OrderQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private OrderEntity orderEntity1;
    private OrderEntity orderEntity2;
    private OrderBo orderBo1;
    private OrderBo orderBo2;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        queryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setCreateStartTime(sevenDaysAgo)
                .setCreateEndTime(now);

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
    void testFindOrders_Success() {
        // Given
        List<OrderEntity> orderEntities = Arrays.asList(orderEntity1, orderEntity2);
        when(orderRepository.findList(queryDto)).thenReturn(orderEntities);
        when(orderMapper.toBo(orderEntity1)).thenReturn(orderBo1);
        when(orderMapper.toBo(orderEntity2)).thenReturn(orderBo2);

        // When
        List<OrderBo> result = orderQueryService.findOrders(queryDto);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ORDER001", result.get(0).getOrderSn());
        assertEquals("ORDER002", result.get(1).getOrderSn());

        verify(orderRepository).findList(queryDto);
        verify(orderMapper).toBo(orderEntity1);
        verify(orderMapper).toBo(orderEntity2);
    }

    @Test
    void testFindOrders_EmptyResult() {
        // Given
        when(orderRepository.findList(queryDto)).thenReturn(Collections.emptyList());

        // When
        List<OrderBo> result = orderQueryService.findOrders(queryDto);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(orderRepository).findList(queryDto);
        verify(orderMapper, never()).toBo(any(OrderEntity.class));
    }

    @Test
    void testFindOrders_NullResult() {
        // Given
        when(orderRepository.findList(queryDto)).thenReturn(null);

        // When
        List<OrderBo> result = orderQueryService.findOrders(queryDto);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(orderRepository).findList(queryDto);
        verify(orderMapper, never()).toBo(any(OrderEntity.class));
    }

    @Test
    void testFindOrders_WithAllParameters() {
        // Given
        OrderQueryDto fullQueryDto = new OrderQueryDto()
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setCreateStartTime(LocalDateTime.now().minusDays(7))
                .setCreateEndTime(LocalDateTime.now())
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setUserId(123);

        List<OrderEntity> orderEntities = List.of(orderEntity1);
        when(orderRepository.findList(fullQueryDto)).thenReturn(orderEntities);
        when(orderMapper.toBo(orderEntity1)).thenReturn(orderBo1);

        // When
        List<OrderBo> result = orderQueryService.findOrders(fullQueryDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ORDER001", result.get(0).getOrderSn());

        verify(orderRepository).findList(fullQueryDto);
        verify(orderMapper).toBo(orderEntity1);
    }

    @Test
    void testFindOrders_RepositoryException() {
        // Given
        when(orderRepository.findList(queryDto)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderQueryService.findOrders(queryDto));

        assertEquals("Database error", exception.getMessage());
        verify(orderRepository).findList(queryDto);
        verify(orderMapper, never()).toBo(any(OrderEntity.class));
    }

    @Test
    void testFindOrders_MapperException() {
        // Given
        List<OrderEntity> orderEntities = List.of(orderEntity1);
        when(orderRepository.findList(queryDto)).thenReturn(orderEntities);
        when(orderMapper.toBo(orderEntity1)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderQueryService.findOrders(queryDto));

        assertEquals("Mapping error", exception.getMessage());
        verify(orderRepository).findList(queryDto);
        verify(orderMapper).toBo(orderEntity1);
    }
}