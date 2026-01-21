package info.zhihui.ems.business.finance.entity.order;

import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class OrderThirdPartyNotificationDto {
    /**
     * 支付渠道
     */
    private PaymentChannelEnum paymentChannel;

    /**
     * 第三方支付渠道返回的数据
     */
    private Object data;
}
