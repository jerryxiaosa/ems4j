package info.zhihui.ems.business.finance.service.order.core;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
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
    PageResult<OrderBo> findOrdersPage(@NotNull OrderQueryDto dto, @NotNull PageParam pageParam);
}
