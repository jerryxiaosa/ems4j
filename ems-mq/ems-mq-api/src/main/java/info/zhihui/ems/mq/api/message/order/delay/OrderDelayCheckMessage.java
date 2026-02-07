package info.zhihui.ems.mq.api.message.order.delay;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotBlank;

/**
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OrderDelayCheckMessage extends BaseDelayMessage {
    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderSn;
}
