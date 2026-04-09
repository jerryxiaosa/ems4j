package info.zhihui.ems.business.order.service.core.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.order.dto.OrderDetailDto;
import info.zhihui.ems.business.order.dto.OrderListDto;
import info.zhihui.ems.business.order.dto.OrderQueryDto;
import info.zhihui.ems.business.order.entity.OrderDetailEnergyTopUpEntity;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.order.mapstruct.OrderMapper;
import info.zhihui.ems.business.order.qo.OrderListItemQo;
import info.zhihui.ems.business.order.qo.OrderQueryQo;
import info.zhihui.ems.business.order.repository.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.order.repository.OrderRepository;
import info.zhihui.ems.business.order.service.core.OrderQueryService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.QueryValueUtil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
    private final OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;

    /**
     * 根据查询条件分页查找订单列表
     *
     * @param dto 订单查询参数对象
     * @param pageParam 分页参数
     * @return 订单业务对象分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<OrderListDto> findOrdersPage(@NotNull OrderQueryDto dto, @NotNull PageParam pageParam) {
        OrderQueryQo queryQo = buildOrderQueryQo(dto);
        PageResult<OrderListDto> pageResult;

        try (Page<OrderListItemQo> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<OrderListItemQo> pageInfo = page.doSelectPageInfo(() -> orderRepository.findList(queryQo));
            pageResult = orderMapper.pageOrderListItemQoToOrderListDto(pageInfo);
        }

        fillMeterInfo(pageResult);

        return pageResult;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetail(@NotEmpty String orderSn) {
        OrderListItemQo detailQo = orderRepository.selectDetailByOrderSn(orderSn);
        if (detailQo == null) {
            throw new NotFoundException("订单信息不存在");
        }
        OrderDetailDto detailDto = orderMapper.toOrderDetailDto(detailQo);
        fillMeterInfo(detailDto);
        return detailDto;
    }

    private void fillMeterInfo(PageResult<OrderListDto> pageResult) {
        if (pageResult == null || pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return;
        }
        List<String> orderSnList = pageResult.getList().stream()
                .filter(Objects::nonNull)
                .filter(item -> OrderTypeEnum.ENERGY_TOP_UP.equals(item.getOrderType()))
                .map(OrderListDto::getOrderSn)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (orderSnList.isEmpty()) {
            return;
        }

        List<OrderDetailEnergyTopUpEntity> detailList = orderDetailEnergyTopUpRepository.findByOrderSnList(orderSnList);
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        Map<String, OrderDetailEnergyTopUpEntity> detailMap = detailList.stream()
                .filter(Objects::nonNull)
                .filter(detail -> StringUtils.hasText(detail.getOrderSn()))
                .collect(Collectors.toMap(OrderDetailEnergyTopUpEntity::getOrderSn, Function.identity(), (left, right) -> left));

        for (OrderListDto item : pageResult.getList()) {
            if (item == null || !StringUtils.hasText(item.getOrderSn())) {
                continue;
            }
            fillMeterInfo(item, detailMap.get(item.getOrderSn()));
        }
    }

    private void fillMeterInfo(OrderListDto orderDto) {
        if (orderDto == null || !StringUtils.hasText(orderDto.getOrderSn())) {
            return;
        }
        if (!OrderTypeEnum.ENERGY_TOP_UP.equals(orderDto.getOrderType())) {
            orderDto.setMeterName(null).setDeviceNo(null);
            return;
        }
        OrderDetailEnergyTopUpEntity detailEntity = orderDetailEnergyTopUpRepository.selectByOrderSn(orderDto.getOrderSn());
        fillMeterInfo(orderDto, detailEntity);
    }

    private void fillMeterInfo(OrderListDto orderDto, OrderDetailEnergyTopUpEntity detailEntity) {
        if (detailEntity == null || !isElectricMeterTopUp(detailEntity)) {
            orderDto.setMeterName(null).setDeviceNo(null);
            return;
        }
        orderDto.setMeterName(detailEntity.getMeterName()).setDeviceNo(detailEntity.getDeviceNo());
    }

    private boolean isElectricMeterTopUp(OrderDetailEnergyTopUpEntity detail) {
        if (detail.getBalanceType() == null) {
            return false;
        }
        return Objects.equals(detail.getBalanceType(), BalanceTypeEnum.ELECTRIC_METER.getCode());
    }

    private OrderQueryQo buildOrderQueryQo(OrderQueryDto dto) {
        String enterpriseNameLike = QueryValueUtil.normalizeLikeValue(dto.getEnterpriseNameLike());
        return new OrderQueryQo()
                .setOrderType(dto.getOrderType() == null ? null : dto.getOrderType().getCode())
                .setOrderStatus(dto.getOrderStatus() == null ? null : dto.getOrderStatus().name())
                .setOrderSnLike(QueryValueUtil.normalizeLikeValue(dto.getOrderSnLike()))
                .setThirdPartySnLike(QueryValueUtil.normalizeLikeValue(dto.getThirdPartySnLike()))
                .setEnterpriseNameLike(enterpriseNameLike)
                .setOwnerType(enterpriseNameLike == null ? null : OwnerTypeEnum.ENTERPRISE.getCode())
                .setCreateStartTime(dto.getCreateStartTime())
                .setCreateEndTime(dto.getCreateEndTime())
                .setPaymentChannel(dto.getPaymentChannel() == null ? null : dto.getPaymentChannel().name());
    }
}
