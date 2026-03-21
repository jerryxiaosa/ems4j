package info.zhihui.ems.mq.api.message.device;

import info.zhihui.ems.mq.api.message.BaseMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 设备命令首次执行消息
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DeviceCommandExecuteMessage extends BaseMessage {

    @NotNull(message = "命令ID不能为空")
    private Integer commandId;
}
