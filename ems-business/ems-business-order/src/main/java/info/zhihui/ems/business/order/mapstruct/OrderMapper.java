package info.zhihui.ems.business.order.mapstruct;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.order.bo.OrderBo;
import info.zhihui.ems.business.order.dto.OrderDetailDto;
import info.zhihui.ems.business.order.dto.OrderListDto;
import info.zhihui.ems.business.order.entity.OrderEntity;
import info.zhihui.ems.business.order.qo.OrderListItemQo;
import info.zhihui.ems.business.order.enums.OrderStatusEnum;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.order.enums.PaymentChannelEnum;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.paging.PageResult;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderType", qualifiedByName = "toOrderType")
    @Mapping(target = "orderStatus", qualifiedByName = "toOrderStatus")
    @Mapping(target = "paymentChannel", qualifiedByName = "toPaymentChannel")
    @Mapping(target = "ownerType", qualifiedByName = "toOwnerType")
    OrderBo toBo(OrderEntity entity);

    @Mapping(target = "meterName", ignore = true)
    @Mapping(target = "deviceNo", ignore = true)
    @Mapping(target = "orderType", qualifiedByName = "toOrderType")
    @Mapping(target = "orderStatus", qualifiedByName = "toOrderStatus")
    @Mapping(target = "paymentChannel", qualifiedByName = "toPaymentChannel")
    @Mapping(target = "ownerType", qualifiedByName = "toOwnerType")
    OrderListDto toOrderListDto(OrderListItemQo qo);

    @InheritConfiguration(name = "toOrderListDto")
    OrderDetailDto toOrderDetailDto(OrderListItemQo qo);

    PageResult<OrderListDto> pageOrderListItemQoToOrderListDto(PageInfo<OrderListItemQo> pageInfo);

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

    @Named("toOwnerType")
    default OwnerTypeEnum toOwnerType(Integer code) {
        return CodeEnum.fromCode(code, OwnerTypeEnum.class);
    }
}
