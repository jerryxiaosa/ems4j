package info.zhihui.ems.business.finance.dto.order.thirdparty.wx;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class WxRefundConfig extends WxPayConfig {
    private String refundNotifyUrl;
}
