package info.zhihui.ems.mq.api.message.order.delay;

import info.zhihui.ems.mq.api.message.BaseMessage;
import jakarta.validation.constraints.Min;
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
    /**
     * 延迟秒数
     */
    @Min(value = 1, message = "延迟秒数不能小于1")
    private long delaySeconds;
}
