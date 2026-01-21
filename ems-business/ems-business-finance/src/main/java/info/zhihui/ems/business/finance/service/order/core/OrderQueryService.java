package info.zhihui.ems.business.finance.service.order.core;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 订单查询服务接口
 *
 * @author jerryxiaosa
 */
public interface OrderQueryService {

    /**
     * 根据查询条件查找订单列表
     *
     * @param dto 订单查询参数对象
     * @return 订单业务对象列表
     */
    List<OrderBo> findOrders(@Valid @NotNull OrderQueryDto dto);
}
