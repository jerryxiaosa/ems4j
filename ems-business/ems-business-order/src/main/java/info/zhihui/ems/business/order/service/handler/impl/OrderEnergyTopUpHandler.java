package info.zhihui.ems.business.order.service.handler.impl;

import info.zhihui.ems.business.order.bo.OrderBo;
import info.zhihui.ems.business.order.dto.ServiceFeeDto;
import info.zhihui.ems.business.order.dto.ServiceFeeRequestDto;
import info.zhihui.ems.business.order.dto.creation.EnergyOrderCreationInfoDto;
import info.zhihui.ems.business.order.dto.creation.EnergyTopUpDto;
import info.zhihui.ems.business.order.dto.creation.OrderCreationInfoDto;
import info.zhihui.ems.business.order.dto.creation.OrderOwnerSnapshotDto;
import info.zhihui.ems.business.order.entity.OrderDetailEnergyTopUpEntity;
import info.zhihui.ems.business.order.entity.OrderEntity;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.order.mapstruct.OrderMapper;
import info.zhihui.ems.business.order.repository.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.order.repository.OrderRepository;
import info.zhihui.ems.business.order.service.fee.ServiceRateService;
import info.zhihui.ems.business.order.service.handler.completion.OrderCompletionHandler;
import info.zhihui.ems.business.order.service.handler.creation.BaseOrderCreationHandler;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.model.MqMessage;
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
import java.math.RoundingMode;
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

    // 当服务费不为0时，最小的服务费
    private static final BigDecimal MIN_SERVICE_AMOUNT = new BigDecimal("0.01");

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
        BigDecimal orderAmount = energyOrderCreationInfoDto.getOrderAmount();
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuntimeException("订单总金额必须大于0");
        }
        if (BalanceTypeEnum.ELECTRIC_METER.equals(detail.getBalanceType()) && detail.getMeterId() == null) {
            throw new BusinessRuntimeException("电表充值时电表ID不能为空");
        }

        // 创建订单实体
        ServiceFeeDto serviceFeeInfo = getServiceFee(new ServiceFeeRequestDto()
                .setOrderOriginalAmount(orderAmount)
                .setOrderType(this.getOrderType()));
        BigDecimal topUpAmount = calculateTopUpAmount(orderAmount, serviceFeeInfo.getServiceAmount());
        OrderEntity orderEntity = buildOrderEntity(energyOrderCreationInfoDto, serviceFeeInfo);
        fillOwnerSnapshot(orderEntity, new OrderOwnerSnapshotDto()
                .setAccountId(detail.getAccountId())
                .setOwnerId(detail.getOwnerId())
                .setOwnerType(detail.getOwnerType())
                .setOwnerName(detail.getOwnerName()));

        if (orderRepository.insert(orderEntity) != 1) {
            throw new BusinessRuntimeException("保存订单失败");
        }

        // 转换为业务对象
        OrderBo orderBo = convertToOrderBo(orderEntity);

        // 保存订单详情
        saveOrderDetail(detail, orderEntity, topUpAmount);

        return orderBo;
    }

    /**
     * 计算服务费
     *
     * @param requestDto 服务费请求参数
     * @return 服务费信息
     */
    @Override
    public ServiceFeeDto getServiceFee(@Valid @NotNull ServiceFeeRequestDto requestDto) {
        BigDecimal serviceRate = calculateServiceRate(requestDto.getOrderOriginalAmount());
        BigDecimal serviceAmount = calculateServiceAmount(requestDto.getOrderOriginalAmount(), serviceRate);

        return new ServiceFeeDto()
                .setServiceRate(serviceRate)
                .setServiceAmount(serviceAmount)
                .setUserPayAmount(requestDto.getOrderOriginalAmount());
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

        BigDecimal serviceRate = serviceRateService.getDefaultServiceRate();
        if (serviceRate.compareTo(BigDecimal.ZERO) < 0 || serviceRate.compareTo(BigDecimal.ONE) >= 0) {
            throw new BusinessRuntimeException("服务费比例需在0%-100%之间，且不能等于100%");
        }
        return serviceRate;
    }

    /**
     * 保存订单详情
     *
     * @param detail      能耗充值订单详情
     * @param orderEntity 订单实体
     */
    private void saveOrderDetail(EnergyTopUpDto detail, OrderEntity orderEntity, BigDecimal topUpAmount) {
        OrderDetailEnergyTopUpEntity detailEnergyTopUpEntity = toOrderDetail(detail, orderEntity, topUpAmount);
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
    private OrderDetailEnergyTopUpEntity toOrderDetail(EnergyTopUpDto detail,
                                                       OrderEntity orderEntity,
                                                       BigDecimal topUpAmount) {
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
                .setDeviceNo(detail.getDeviceNo())
                .setSpaceId(detail.getSpaceId())
                .setTopUpAmount(Objects.requireNonNull(topUpAmount, "实际充值到账金额不能为空"))
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

    private BigDecimal calculateTopUpAmount(BigDecimal orderAmount, BigDecimal serviceAmount) {
        BigDecimal topUpAmount = Objects.requireNonNull(orderAmount, "订单总金额不能为空")
                .subtract(Objects.requireNonNull(serviceAmount, "服务费金额不能为空"));
        if (topUpAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuntimeException("订单金额过小，扣除服务费后实际充值到账金额必须大于0");
        }
        return topUpAmount;
    }

    private BigDecimal calculateServiceAmount(BigDecimal orderAmount, BigDecimal serviceRate) {
        if (serviceRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        }

        BigDecimal serviceAmount = orderAmount.multiply(serviceRate).setScale(2, RoundingMode.DOWN);
        return serviceAmount.compareTo(MIN_SERVICE_AMOUNT) < 0 ? MIN_SERVICE_AMOUNT : serviceAmount;
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
                        .setTopUpAmount(orderDetailEnergyTopUpEntity.getTopUpAmount())
                        .setBalanceType(CodeEnum.fromCode(orderDetailEnergyTopUpEntity.getBalanceType(), BalanceTypeEnum.class))
                        .setAccountId(orderDetailEnergyTopUpEntity.getAccountId())
                        .setMeterId(orderDetailEnergyTopUpEntity.getMeterId())
                        .setMeterType(CodeEnum.fromCode(orderDetailEnergyTopUpEntity.getMeterType(), MeterTypeEnum.class))
                        .setOrderSn(orderBo.getOrderSn())
                        .setOrderStatus(orderBo.getOrderStatus().name())
                );
    }

}
