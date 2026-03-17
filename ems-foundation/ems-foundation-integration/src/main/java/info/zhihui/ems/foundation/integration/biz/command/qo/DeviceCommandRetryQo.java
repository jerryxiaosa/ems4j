package info.zhihui.ems.foundation.integration.biz.command.qo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设备命令重试查询参数
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class DeviceCommandRetryQo {

    /**
     * 最大执行次数
     */
    private Integer maxExecuteTimes;

    /**
     * 查询数量限制
     */
    private Integer fetchSize;
}
