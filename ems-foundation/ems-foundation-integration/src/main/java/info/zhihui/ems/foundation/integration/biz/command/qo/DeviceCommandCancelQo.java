package info.zhihui.ems.foundation.integration.biz.command.qo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class DeviceCommandCancelQo {
    private Integer deviceId;
    private String deviceType;
    private Integer commandType;
    private String reason;
}
