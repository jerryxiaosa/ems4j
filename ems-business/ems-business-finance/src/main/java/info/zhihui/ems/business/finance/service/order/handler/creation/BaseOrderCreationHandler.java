package info.zhihui.ems.business.finance.service.order.handler.creation;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.ServiceFeeDto;
import info.zhihui.ems.business.finance.dto.order.ServiceFeeRequestDto;
import info.zhihui.ems.business.finance.dto.order.creation.OrderCreationInfoDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.common.utils.SerialNumberGeneratorUtil;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 订单创建处理器通用基础类，封装 OrderEntity 到 OrderBo 的转换等公共逻辑。
 */
public abstract class BaseOrderCreationHandler implements OrderCreationHandler {

    private final OrderMapper orderMapper;
    private final static String DEFAULT_CURRENCY = "CNY";
    private static final long PAYMENT_TIMEOUT_MINUTES = 15L;

    public BaseOrderCreationHandler(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 获取订单类型
     *
     * @return 订单类型
     */
    protected abstract OrderTypeEnum getOrderType();

    /**
     * 获取服务费
     *
     * @param requestDto 请求参数
     * @return 服务费
     */
    protected abstract ServiceFeeDto getServiceFee(ServiceFeeRequestDto requestDto);

    protected OrderBo convertToOrderBo(OrderEntity entity) {
        return orderMapper.toBo(entity);
    }

    protected String getDefaultCurrency() {
        return DEFAULT_CURRENCY;
    }

    protected String generateOrderSerialNumber(OrderTypeEnum orderType) {
        return SerialNumberGeneratorUtil.genOrderSn(orderType.getCode());
    }

    protected OrderEntity buildOrderEntity(OrderCreationInfoDto orderCreationInfoDto,
                                           ServiceFeeDto serviceFeeInfo
                                           ) {
        Objects.requireNonNull(orderCreationInfoDto, "订单参数不能为空");
        ServiceFeeDto feeInfo = Objects.requireNonNull(serviceFeeInfo, "服务费信息不能为空");

        LocalDateTime currentTime = LocalDateTime.now();
        OrderTypeEnum orderType = this.getOrderType();

        return new OrderEntity()
                .setOrderSn(generateOrderSerialNumber(orderType))
                .setUserId(orderCreationInfoDto.getUserId())
                .setUserPhone(orderCreationInfoDto.getUserPhone())
                .setUserRealName(orderCreationInfoDto.getUserRealName())
                .setThirdPartyUserId(orderCreationInfoDto.getThirdPartyUserId())
                .setOrderType(orderType.getCode())
                .setOrderAmount(orderCreationInfoDto.getOrderAmount())
                .setCurrency(getDefaultCurrency())
                .setServiceRate(feeInfo.getServiceRate())
                .setServiceAmount(feeInfo.getServiceAmount())
                .setUserPayAmount(feeInfo.getUserPayAmount())
                .setPaymentChannel(Objects.requireNonNull(orderCreationInfoDto.getPaymentChannel(), "支付渠道不能为空").name())
                .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
                .setOrderCreateTime(currentTime)
                .setOrderPayStopTime(currentTime.plusMinutes(PAYMENT_TIMEOUT_MINUTES));
    }

}
