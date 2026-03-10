package info.zhihui.ems.business.order.service.core;

import info.zhihui.ems.business.order.dto.OrderDetailDto;
import info.zhihui.ems.business.order.dto.OrderListDto;
import info.zhihui.ems.business.order.dto.OrderQueryDto;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 订单查询服务接口
 *
 * @author jerryxiaosa
 */
public interface OrderQueryService {

    /**
     * 根据查询条件分页查找订单列表
     *
     * @param dto 订单查询参数对象
     * @param pageParam 分页参数
     * @return 订单业务对象分页结果
     */
    PageResult<OrderListDto> findOrdersPage(@NotNull OrderQueryDto dto, @NotNull PageParam pageParam);

    /**
     * 获取订单详情
     *
     * @param orderSn 订单编号
     * @return 订单详情
     */
    OrderDetailDto getOrderDetail(@NotEmpty String orderSn);
}
