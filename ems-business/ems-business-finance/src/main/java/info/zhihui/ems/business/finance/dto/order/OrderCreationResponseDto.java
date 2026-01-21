package info.zhihui.ems.business.finance.dto.order;

import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class OrderCreationResponseDto {
    private String orderSn;

    private OrderTypeEnum orderTypeEnum;

    private PaymentChannelEnum paymentChannel;

    private LocalDateTime orderPayStopTime;
}
