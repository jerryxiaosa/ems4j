package info.zhihui.ems.business.finance.mapstruct;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.common.enums.CodeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderType", qualifiedByName = "toOrderType")
    @Mapping(target = "orderStatus", qualifiedByName = "toOrderStatus")
    @Mapping(target = "paymentChannel", qualifiedByName = "toPaymentChannel")
    OrderBo toBo(OrderEntity entity);

    @Named("toOrderType")
    default OrderTypeEnum toOrderType(Integer code) {
        return CodeEnum.fromCode(code, OrderTypeEnum.class);
    }

    @Named("toOrderStatus")
    default OrderStatusEnum toOrderStatus(String status) {
        if(status == null) {
             return null;
        }

        try {
            return OrderStatusEnum.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    @Named("toPaymentChannel")
    default PaymentChannelEnum toPaymentChannel(String channel) {
        if (channel == null) {
            return null;
        }
        try {
            return PaymentChannelEnum.valueOf(channel);
        } catch (IllegalArgumentException e) {
            return null;
        }

    }
}
