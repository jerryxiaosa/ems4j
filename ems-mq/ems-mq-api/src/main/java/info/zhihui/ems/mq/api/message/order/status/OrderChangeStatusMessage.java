package info.zhihui.ems.mq.api.message.order.status;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OrderChangeStatusMessage extends BaseOrderStatusMessage {
}
