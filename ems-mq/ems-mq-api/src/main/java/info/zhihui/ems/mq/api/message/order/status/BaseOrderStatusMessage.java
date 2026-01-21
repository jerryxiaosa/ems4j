package info.zhihui.ems.mq.api.message.order.status;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public abstract class BaseOrderStatusMessage {
    @NotNull(message = "订单编号不能为空")
    private String orderSn;

    @NotEmpty(message = "订单状态不能为空")
    private String orderStatus;
}
