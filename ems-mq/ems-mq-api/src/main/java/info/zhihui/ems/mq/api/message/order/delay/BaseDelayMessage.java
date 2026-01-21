package info.zhihui.ems.mq.api.message.order.delay;

import info.zhihui.ems.mq.api.message.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class BaseDelayMessage extends BaseMessage {
    private long delaySeconds;
}
