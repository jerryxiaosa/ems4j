package info.zhihui.ems.business.finance.service.order.core.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.qo.OrderQueryQo;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.OrderQueryService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单查询服务接口
 *
 * @author jerryxiaosa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /**
     * 根据查询条件分页查找订单列表
     *
     * @param dto 订单查询参数对象
     * @param pageParam 分页参数
     * @return 订单业务对象分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<OrderBo> findOrdersPage(@NotNull OrderQueryDto dto, @NotNull PageParam pageParam) {
        OrderQueryQo queryQo = buildOrderQueryQo(dto);

        try (Page<OrderEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<OrderEntity> pageInfo = page.doSelectPageInfo(() -> orderRepository.findList(queryQo));
            return orderMapper.pageEntityToBo(pageInfo);
        }
    }

    private OrderQueryQo buildOrderQueryQo(OrderQueryDto dto) {
        return new OrderQueryQo()
                .setOrderStatus(dto.getOrderStatus() == null ? null : dto.getOrderStatus().name())
                .setCreateStartTime(dto.getCreateStartTime())
                .setCreateEndTime(dto.getCreateEndTime())
                .setPaymentChannel(dto.getPaymentChannel() == null ? null : dto.getPaymentChannel().name())
                .setUserId(dto.getUserId());
    }
}
