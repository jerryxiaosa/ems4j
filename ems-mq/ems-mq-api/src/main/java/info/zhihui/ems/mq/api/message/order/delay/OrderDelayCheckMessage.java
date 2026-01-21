package info.zhihui.ems.mq.api.message.order.delay;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OrderDelayCheckMessage extends BaseDelayMessage {
    private String orderSn;
}
