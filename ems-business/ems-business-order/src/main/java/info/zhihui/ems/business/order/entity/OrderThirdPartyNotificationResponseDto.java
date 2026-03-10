package info.zhihui.ems.business.order.entity;

import info.zhihui.ems.business.order.enums.OrderStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class OrderThirdPartyNotificationResponseDto {
    private String orderSn;
    private OrderStatusEnum orderStatus;
    private BigDecimal payOrderAmount;
}
