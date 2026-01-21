package info.zhihui.ems.business.finance.dto.order;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 微信小程序支付返回。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class WxOrderCreationResponseDto extends OrderCreationResponseDto {

    /**
     * 小程序预支付返回
     */
    private PrepayWithRequestPaymentResponse prepayWithRequestPaymentResponse;
}
