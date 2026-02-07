package info.zhihui.ems.mq.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * MQ 消息封装
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class MqMessage {
    /**
     * 交换机/主题
     */
    @NotBlank(message = "消息目的地不能为空")
    private String messageDestination;

    /**
     * 路由标识
     */
    @NotBlank(message = "路由标识不能为空")
    private String routingIdentifier;

    /**
     * 消息载荷
     */
    @NotNull(message = "消息载荷不能为空")
    private Object payload;
}
