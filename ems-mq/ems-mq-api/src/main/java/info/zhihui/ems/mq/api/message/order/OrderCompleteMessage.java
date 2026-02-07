package info.zhihui.ems.mq.api.message.order;

import info.zhihui.ems.mq.api.message.BaseMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OrderCompleteMessage extends BaseMessage {
    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderSn;
}
