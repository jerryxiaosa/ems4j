package info.zhihui.ems.business.finance.service.order.core.impl;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.OrderQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
     * 根据查询条件查找订单列表
     *
     * @param dto 订单查询参数对象
     * @return 订单业务对象列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderBo> findOrders(@Valid @NotNull OrderQueryDto dto) {
        log.info("查询订单列表，参数：{}", dto);

        List<OrderEntity> orderEntities = orderRepository.findList(dto);

        if (orderEntities == null || orderEntities.isEmpty()) {
            log.info("未查询到符合条件的订单");
            return List.of();
        }

        List<OrderBo> orderBos = orderEntities.stream()
                .map(orderMapper::toBo)
                .collect(Collectors.toList());

        log.info("查询到{}条订单记录", orderBos.size());
        return orderBos;
    }
}