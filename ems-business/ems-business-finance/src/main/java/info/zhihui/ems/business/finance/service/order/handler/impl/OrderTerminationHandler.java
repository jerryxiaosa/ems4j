package info.zhihui.ems.business.finance.service.order.handler.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.ServiceFeeDto;
import info.zhihui.ems.business.finance.dto.order.ServiceFeeRequestDto;
import info.zhihui.ems.business.finance.dto.order.creation.OrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationOrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationSettlementDto;
import info.zhihui.ems.business.finance.entity.order.OrderDetailTerminationEntity;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderDetailTerminationRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.handler.completion.OrderCompletionHandler;
import info.zhihui.ems.business.finance.service.order.handler.creation.BaseOrderCreationHandler;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.api.message.order.status.TerminationSuccessMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 销户/销表订单创建处理器。
 */
@Component
@Validated
public class OrderTerminationHandler extends BaseOrderCreationHandler implements OrderCompletionHandler {

    private final OrderRepository orderRepository;
    private final OrderDetailTerminationRepository orderDetailTerminationRepository;

    @Autowired
    public OrderTerminationHandler(OrderMapper orderMapper,
                                   OrderRepository orderRepository,
                                   OrderDetailTerminationRepository orderDetailTerminationRepository) {
        super(orderMapper);
        this.orderDetailTerminationRepository = orderDetailTerminationRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 获取支持的订单参数
     *
     * @return 订单参数
     */
    @Override
    public Class<? extends OrderCreationInfoDto> getSupportedParam() {
        return TerminationOrderCreationInfoDto.class;
    }

    /**
     * 获取订单类型
     *
     * @return 订单类型
     */
    @Override
    public OrderTypeEnum getOrderType() {
        return OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT;
    }

    /**
     * 计算服务费
     *
     * @param requestDto 服务费请求参数
     * @return 服务费信息
     */
    @Override
    protected ServiceFeeDto getServiceFee(ServiceFeeRequestDto requestDto) {
        return new ServiceFeeDto()
                .setServiceRate(BigDecimal.ZERO)
                .setServiceAmount(BigDecimal.ZERO)
                .setOrderOriginalAmount(requestDto.getOrderOriginalAmount())
                .setUserPayAmount(requestDto.getOrderOriginalAmount());
    }

    /**
     * 创建订单
     *
     * @param orderCreationInfoDto 订单参数
     * @return 订单信息
     */
    @Override
    public OrderBo createOrder(@Valid @NotNull OrderCreationInfoDto orderCreationInfoDto) {
        if (!(orderCreationInfoDto instanceof TerminationOrderCreationInfoDto terminationInfoDto)) {
            throw new BusinessRuntimeException("不支持的订单参数类型");
        }

        TerminationSettlementDto settlementDto = terminationInfoDto.getTerminationInfo();

        // @NOTICE 目前销户只能线下退款
        terminationInfoDto.setPaymentChannel(PaymentChannelEnum.OFFLINE);
        BigDecimal orderAmount = terminationInfoDto.getOrderAmount();
        if (orderAmount.compareTo(settlementDto.getSettlementAmount()) != 0) {
            throw new BusinessRuntimeException("订单金额与结算金额不一致");
        }

        ServiceFeeDto serviceFeeInfo = getServiceFee(new ServiceFeeRequestDto().setOrderOriginalAmount(orderAmount));
        OrderEntity orderEntity = buildOrderEntity(terminationInfoDto, serviceFeeInfo);
        if (orderRepository.insert(orderEntity) != 1) {
            throw new BusinessRuntimeException("保存订单失败");
        }

        saveOrderDetail(settlementDto, orderEntity);

        return convertToOrderBo(orderEntity);
    }

    private void saveOrderDetail(TerminationSettlementDto settlementDto, OrderEntity orderEntity) {
        String orderSn = Objects.requireNonNull(orderEntity.getOrderSn(), "订单号不能为空");
        OrderDetailTerminationEntity entity = new OrderDetailTerminationEntity()
                .setOrderSn(orderSn)
                .setCancelNo(settlementDto.getCancelNo())
                .setAccountId(settlementDto.getAccountId())
                .setOwnerId(settlementDto.getOwnerId())
                .setOwnerType(settlementDto.getOwnerType() == null ? null : settlementDto.getOwnerType().getCode())
                .setOwnerName(settlementDto.getOwnerName())
                .setSettlementAmount(settlementDto.getSettlementAmount())
                .setFullCancel(settlementDto.getFullCancel())
                .setElectricMeterAmount(settlementDto.getElectricMeterAmount())
                .setElectricAccountType(settlementDto.getElectricAccountType() == null ? null : settlementDto.getElectricAccountType().getCode())
                .setCloseReason(settlementDto.getCloseReason())
                .setSnapshotPayload(JacksonUtil.toJson(settlementDto.getMeterIdList()))
                .setCreateTime(Objects.requireNonNull(orderEntity.getOrderCreateTime(), "订单创建时间不能为空"));
        if (orderDetailTerminationRepository.insert(entity) != 1) {
            throw new BusinessRuntimeException("保存销户订单详情失败");
        }
    }

    /**
     * 订单成功后创建MQ消息
     *
     * @param orderBo 订单信息
     * @return MQ消息
     */
    @Override
    public MqMessage createMessageAfterOrderSuccess(OrderBo orderBo) {
        OrderDetailTerminationEntity terminationEntity = orderDetailTerminationRepository.selectByOrderSn(orderBo.getOrderSn());
        if (terminationEntity == null) {
            throw new BusinessRuntimeException("数据异常，订单详情不存在");
        }
        List<Integer> meterIdList = JacksonUtil.fromJson(terminationEntity.getSnapshotPayload(), new TypeReference<>() {});

        return new MqMessage()
                .setMessageDestination(OrderConstant.ORDER_DESTINATION)
                .setRoutingIdentifier(OrderConstant.ROUTING_KEY_ORDER_STATUS_SUCCESS_TERMINATION)
                .setPayload(new TerminationSuccessMessage()
                        .setAccountId(terminationEntity.getAccountId())
                        .setFullCancel(terminationEntity.getFullCancel())
                        .setElectricAccountType(CodeEnum.fromCode(terminationEntity.getElectricAccountType(), ElectricAccountTypeEnum.class))
                        .setElectricMeterAmount(terminationEntity.getElectricMeterAmount())
                        .setMeterIdList(meterIdList)
                        .setOrderSn(orderBo.getOrderSn())
                        .setOrderStatus(orderBo.getOrderStatus().name())
                );
    }

}
