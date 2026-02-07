package info.zhihui.ems.business.finance.service.order.core;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.service.order.core.impl.OrderCheckServiceImpl;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderCheckServiceImpl 单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class OrderCheckServiceImplTest {

    @Mock
    private OrderQueryService orderQueryService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderCheckServiceImpl orderCheckService;

    private OrderBo orderBo1;
    private OrderBo orderBo2;
    private OrderBo orderBo3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        orderBo1 = new OrderBo();
        orderBo1.setOrderSn("ORDER001");
        orderBo1.setOrderStatus(OrderStatusEnum.NOT_PAY);
        orderBo1.setOrderCreateTime(now.minusDays(3));

        orderBo2 = new OrderBo();
        orderBo2.setOrderSn("ORDER002");
        orderBo2.setOrderStatus(OrderStatusEnum.NOT_PAY);
        orderBo2.setOrderCreateTime(now.minusDays(5));

        orderBo3 = new OrderBo();
        orderBo3.setOrderSn("ORDER003");
        orderBo3.setOrderStatus(OrderStatusEnum.NOT_PAY);
        orderBo3.setOrderCreateTime(now.minusDays(1));
    }

    @Test
    void testCompletePendingOrdersInLast7Days_Success() {
        // Given
        List<OrderBo> pendingOrders = Arrays.asList(orderBo1, orderBo2, orderBo3);
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class))).thenReturn(mockPageResult(pendingOrders));

        // When
        assertDoesNotThrow(() -> orderCheckService.completePendingOrdersInLast7Days());

        // Then
        verify(orderQueryService).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService).complete("ORDER001");
        verify(orderService).complete("ORDER002");
        verify(orderService).complete("ORDER003");
    }

    @Test
    void testCompletePendingOrdersInLast7Days_EmptyOrders() {
        // Given
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class))).thenReturn(mockPageResult(Collections.emptyList()));

        // When
        assertDoesNotThrow(() -> orderCheckService.completePendingOrdersInLast7Days());

        // Then
        verify(orderQueryService).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService, never()).complete(any(String.class));
    }

    @Test
    void testCompletePendingOrdersInLast7Days_NullOrders() {
        // Given
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class))).thenReturn(null);

        // When
        assertDoesNotThrow(() -> orderCheckService.completePendingOrdersInLast7Days());

        // Then
        verify(orderQueryService).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService, never()).complete(any(String.class));
    }

    @Test
    void testCompletePendingOrdersInLast7Days_PartialFailure() {
        // Given
        List<OrderBo> pendingOrders = Arrays.asList(orderBo1, orderBo2, orderBo3);
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class))).thenReturn(mockPageResult(pendingOrders));

        // 模拟第二个订单完成失败
        doNothing().when(orderService).complete("ORDER001");
        doThrow(new RuntimeException("Complete failed")).when(orderService).complete("ORDER002");
        doNothing().when(orderService).complete("ORDER003");

        // When
        assertDoesNotThrow(() -> orderCheckService.completePendingOrdersInLast7Days());

        // Then
        verify(orderQueryService).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService).complete("ORDER001");
        verify(orderService).complete("ORDER002");
        verify(orderService).complete("ORDER003");
    }

    @Test
    void testCompletePendingOrdersInLast7Days_AllOrdersFailure() {
        // Given
        List<OrderBo> pendingOrders = Arrays.asList(orderBo1, orderBo2);
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class))).thenReturn(mockPageResult(pendingOrders));

        // 模拟所有订单完成都失败
        doThrow(new RuntimeException("Complete failed 1")).when(orderService).complete("ORDER001");
        doThrow(new RuntimeException("Complete failed 2")).when(orderService).complete("ORDER002");

        // When
        assertDoesNotThrow(() -> orderCheckService.completePendingOrdersInLast7Days());

        // Then
        verify(orderQueryService).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService).complete("ORDER001");
        verify(orderService).complete("ORDER002");
    }

    @Test
    void testCompletePendingOrdersInLast7Days_QueryServiceException() {
        // Given
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class)))
                .thenThrow(new RuntimeException("Query service error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderCheckService.completePendingOrdersInLast7Days());

        assertEquals("Query service error", exception.getMessage());
        verify(orderQueryService).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService, never()).complete(any(String.class));
    }

    @Test
    void testCompletePendingOrdersInLast7Days_VerifyQueryParameters() {
        // Given
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class))).thenReturn(mockPageResult(Collections.emptyList()));

        // When
        orderCheckService.completePendingOrdersInLast7Days();

        // Then
        verify(orderQueryService).findOrdersPage(argThat(queryDto -> {
                    assertNotNull(queryDto);
                    assertEquals(OrderStatusEnum.NOT_PAY, queryDto.getOrderStatus());
                    assertNotNull(queryDto.getCreateStartTime());
                    assertNotNull(queryDto.getCreateEndTime());

                    // 验证时间范围是过去7天
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime sevenDaysAgo = now.minusDays(7);

                    // 允许一定的时间误差（1分钟）
                    assertTrue(queryDto.getCreateStartTime().isAfter(sevenDaysAgo.minusMinutes(1)));
                    assertTrue(queryDto.getCreateStartTime().isBefore(sevenDaysAgo.plusMinutes(1)));
                    assertTrue(queryDto.getCreateEndTime().isAfter(now.minusMinutes(1)));
                    assertTrue(queryDto.getCreateEndTime().isBefore(now.plusMinutes(1)));
                    return true;
                }),
                argThat(pageParam -> {
                    assertNotNull(pageParam);
                    assertEquals(1, pageParam.getPageNum());
                    assertEquals(200, pageParam.getPageSize());
                    return true;
                }));
    }

    @Test
    void testCompletePendingOrdersInLast7Days_OrderWithNullOrderSn() {
        // Given
        OrderBo orderWithNullSn = new OrderBo();
        orderWithNullSn.setOrderSn(null);
        orderWithNullSn.setOrderStatus(OrderStatusEnum.NOT_PAY);

        List<OrderBo> pendingOrders = Arrays.asList(orderBo1, orderWithNullSn, orderBo2);
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class))).thenReturn(mockPageResult(pendingOrders));

        // When
        assertDoesNotThrow(() -> orderCheckService.completePendingOrdersInLast7Days());

        // Then
        verify(orderQueryService).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService).complete("ORDER001");
        verify(orderService, never()).complete(null);
        verify(orderService).complete("ORDER002");
    }

    @Test
    void testCompletePendingOrdersInLast7Days_OrderWithEmptyOrderSn() {
        // Given
        OrderBo orderWithEmptySn = new OrderBo();
        orderWithEmptySn.setOrderSn("");
        orderWithEmptySn.setOrderStatus(OrderStatusEnum.NOT_PAY);

        List<OrderBo> pendingOrders = Arrays.asList(orderBo1, orderWithEmptySn, orderBo2);
        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class))).thenReturn(mockPageResult(pendingOrders));

        // When
        assertDoesNotThrow(() -> orderCheckService.completePendingOrdersInLast7Days());

        // Then
        verify(orderQueryService).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService).complete("ORDER001");
        verify(orderService, never()).complete("");
        verify(orderService).complete("ORDER002");
    }

    @Test
    void testCompletePendingOrdersInLast7Days_PaginationLoop() {
        List<OrderBo> firstPageOrders = IntStream.range(0, 200)
                .mapToObj(index -> new OrderBo()
                        .setOrderSn("ORDER_" + index)
                        .setOrderStatus(OrderStatusEnum.NOT_PAY)
                        .setOrderCreateTime(LocalDateTime.now().minusHours(1)))
                .toList();

        when(orderQueryService.findOrdersPage(any(OrderQueryDto.class), any(PageParam.class)))
                .thenReturn(mockPageResult(firstPageOrders))
                .thenReturn(mockPageResult(Collections.emptyList()));

        assertDoesNotThrow(() -> orderCheckService.completePendingOrdersInLast7Days());

        verify(orderQueryService, times(2)).findOrdersPage(any(OrderQueryDto.class), any(PageParam.class));
        verify(orderService, times(200)).complete(any(String.class));
    }

    private PageResult<OrderBo> mockPageResult(List<OrderBo> orders) {
        return new PageResult<OrderBo>().setList(orders);
    }
}
