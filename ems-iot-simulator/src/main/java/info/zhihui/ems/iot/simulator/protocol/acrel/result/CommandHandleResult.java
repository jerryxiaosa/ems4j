package info.zhihui.ems.iot.simulator.protocol.acrel.result;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设备侧命令处理结果。
 */
@Data
@Accessors(chain = true)
public class CommandHandleResult {

    private boolean handled;
    private String commandName;
    private byte[] responseFrame;
}
