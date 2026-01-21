package info.zhihui.ems.business.finance.service.order.handler.impl;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.ServiceFeeDto;
import info.zhihui.ems.business.finance.dto.order.ServiceFeeRequestDto;
import info.zhihui.ems.business.finance.dto.order.creation.EnergyOrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.EnergyTopUpDto;
import info.zhihui.ems.business.finance.dto.order.creation.OrderCreationInfoDto;
import info.zhihui.ems.business.finance.entity.order.OrderDetailEnergyTopUpEntity;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.fee.ServiceRateService;
import info.zhihui.ems.business.finance.service.order.handler.completion.OrderCompletionHandler;
import info.zhihui.ems.business.finance.service.order.handler.creation.BaseOrderCreationHandler;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.model.MqMessage;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.foundation.space.util.SpaceInfoUtils;
import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.api.message.order.status.EnergyTopUpSuccessMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 能耗充值订单创建处理器
 * 负责处理能耗充值订单的创建逻辑，包括订单信息构建、服务费计算、订单详情保存等功能
 *
 * @author jerryxiaosa
 */
@Slf4j
@Component
@Validated
public class OrderEnergyTopUpHandler extends BaseOrderCreationHandler implements OrderCompletionHandler {

    private final OrderRepository orderRepository;
    private final OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;
    private final SpaceService spaceService;
    private final ServiceRateService serviceRateService;

    public OrderEnergyTopUpHandler(OrderMapper orderMapper,
                                   OrderRepository orderRepository,
                                   OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository,
                                   ServiceRateService serviceRateService,
                                   SpaceService spaceService) {
        super(orderMapper);
        this.orderRepository = orderRepository;
        this.orderDetailEnergyTopUpRepository = orderDetailEnergyTopUpRepository;
        this.serviceRateService = serviceRateService;
        this.spaceService = spaceService;
    }

    /**
     * 获取支持的订单参数
     *
     * @return 订单参数
     */
    @Override
    public Class<? extends OrderCreationInfoDto> getSupportedParam() {
        return EnergyOrderCreationInfoDto.class;
    }

    /**
     * 获取订单类型
     *
     * @return 订单类型
     */
    @Override
    public OrderTypeEnum getOrderType() {
        return OrderTypeEnum.ENERGY_TOP_UP;
    }

    /**
     * 创建订单
     *
     * @param orderCreationInfoDto 订单参数
     * @return 订单信息
     */
    @Override
    public OrderBo createOrder(@Valid @NotNull OrderCreationInfoDto orderCreationInfoDto) {
        if (!(orderCreationInfoDto instanceof EnergyOrderCreationInfoDto energyOrderCreationInfoDto)) {
            throw new BusinessRuntimeException("不支持的订单参数类型");
        }

        EnergyTopUpDto detail = energyOrderCreationInfoDto.getEnergyTopUpDto();
        if (BalanceTypeEnum.ELECTRIC_METER.equals(detail.getBalanceType()) && detail.getMeterId() == null) {
            throw new BusinessRuntimeException("电表充值时电表ID不能为空");
        }

        // 创建订单实体
        ServiceFeeDto serviceFeeInfo = getServiceFee(new ServiceFeeRequestDto()
                .setOrderOriginalAmount(energyOrderCreationInfoDto.getOrderAmount())
                .setOrderType(this.getOrderType()));
        OrderEntity orderEntity = buildOrderEntity(energyOrderCreationInfoDto, serviceFeeInfo);

        if (orderRepository.insert(orderEntity) != 1) {
            throw new BusinessRuntimeException("保存订单失败");
        }

        // 转换为业务对象
        OrderBo orderBo = convertToOrderBo(orderEntity);

        // 保存订单详情
        saveOrderDetail(detail, orderEntity);

        return orderBo;
    }

    /**
     * 计算服务费
     *
     * @param requestDto 服务费请求参数
     * @return 服务费信息
     */
    @Override
    public ServiceFeeDto getServiceFee(ServiceFeeRequestDto requestDto) {
        BigDecimal serviceRate = calculateServiceRate(requestDto.getOrderOriginalAmount());
        BigDecimal serviceAmount = requestDto.getOrderOriginalAmount().multiply(serviceRate);
        BigDecimal userPayAmount = requestDto.getOrderOriginalAmount().add(serviceAmount);

        return new ServiceFeeDto()
                .setServiceRate(serviceRate)
                .setServiceAmount(serviceAmount)
                .setOrderOriginalAmount(requestDto.getOrderOriginalAmount())
                .setUserPayAmount(userPayAmount);
    }

    /**
     * 计算服务费率
     *
     * @param orderAmount 订单金额
     * @return 服务费率
     */
    private BigDecimal calculateServiceRate(BigDecimal orderAmount) {
        if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return serviceRateService.getDefaultServiceRate();
    }

    /**
     * 保存订单详情
     *
     * @param detail      能耗充值订单详情
     * @param orderEntity 订单实体
     */
    private void saveOrderDetail(EnergyTopUpDto detail, OrderEntity orderEntity) {
        OrderDetailEnergyTopUpEntity detailEnergyTopUpEntity = toOrderDetail(detail, orderEntity);
        if (orderDetailEnergyTopUpRepository.insert(detailEnergyTopUpEntity) != 1) {
            throw new BusinessRuntimeException("保存能耗订单详情失败");
        }
    }

    /**
     * 将能耗充值订单详情转换为实体
     *
     * @param detail      能耗充值订单详情
     * @param orderEntity 订单实体
     * @return 订单详情实体
     */
    private OrderDetailEnergyTopUpEntity toOrderDetail(EnergyTopUpDto detail, OrderEntity orderEntity) {
        String orderSn = Objects.requireNonNull(orderEntity.getOrderSn(), "订单号不能为空");
        OrderDetailEnergyTopUpEntity orderDetailEnergyTopUpEntity = new OrderDetailEnergyTopUpEntity()
                .setOrderSn(orderSn)
                .setOwnerId(detail.getOwnerId())
                .setOwnerType(resolveEnumCode(detail.getOwnerType()))
                .setOwnerName(detail.getOwnerName())
                .setAccountId(detail.getAccountId())
                .setBalanceType(resolveEnumCode(detail.getBalanceType()))
                .setElectricAccountType(resolveEnumCode(detail.getElectricAccountType()))
                .setMeterType(resolveEnumCode(detail.getMeterType()))
                .setMeterId(detail.getMeterId())
                .setMeterName(detail.getMeterName())
                .setMeterNo(detail.getMeterNo())
                .setSpaceId(detail.getSpaceId())
                .setCreateTime(orderEntity.getOrderCreateTime());

        SpaceInfoUtils.fillFromSpaceId(
                detail.getSpaceId(),
                spaceService,
                orderDetailEnergyTopUpEntity::setSpaceName,
                orderDetailEnergyTopUpEntity::setSpaceParentIds,
                orderDetailEnergyTopUpEntity::setSpaceParentNames
        );

        return orderDetailEnergyTopUpEntity;
    }

    private Integer resolveEnumCode(CodeEnum<Integer> codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }


    /**
     * 订单成功后创建MQ消息
     *
     * @param orderBo 订单信息
     * @return MQ消息
     */
    @Override
    public MqMessage createMessageAfterOrderSuccess(OrderBo orderBo) {
        OrderDetailEnergyTopUpEntity orderDetailEnergyTopUpEntity = orderDetailEnergyTopUpRepository.selectByOrderSn(orderBo.getOrderSn());
        if (orderDetailEnergyTopUpEntity == null) {
            throw new BusinessRuntimeException("数据异常，订单详情不存在");
        }

        return new MqMessage()
                .setMessageDestination(OrderConstant.ORDER_DESTINATION)
                .setRoutingIdentifier(OrderConstant.ROUTING_KEY_ORDER_STATUS_SUCCESS_ENERGY_TOP_UP)
                .setPayload(new EnergyTopUpSuccessMessage()
                        .setOrderAmount(orderBo.getOrderAmount())
                        .setBalanceType(CodeEnum.fromCode(orderDetailEnergyTopUpEntity.getBalanceType(), BalanceTypeEnum.class))
                        .setAccountId(orderDetailEnergyTopUpEntity.getAccountId())
                        .setMeterId(orderDetailEnergyTopUpEntity.getMeterId())
                        .setMeterType(CodeEnum.fromCode(orderDetailEnergyTopUpEntity.getMeterType(), MeterTypeEnum.class))
                        .setOrderSn(orderBo.getOrderSn())
                        .setOrderStatus(orderBo.getOrderStatus().name())
                );
    }

}
