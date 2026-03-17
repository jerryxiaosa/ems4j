package info.zhihui.ems.foundation.integration.biz.command.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 设备命令开始执行参数
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class DeviceCommandStartExecuteQo {

    /**
     * 命令 ID
     */
    private Integer commandId;

    /**
     * 最大执行次数
     */
    private Integer maxExecuteTimes;

    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;
}
