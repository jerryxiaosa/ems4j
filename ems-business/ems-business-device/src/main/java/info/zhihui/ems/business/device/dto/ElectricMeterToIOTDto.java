package info.zhihui.ems.business.device.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 *
 * nb表设置imme
 * 非nb通过网关相连，设置网关相关的参数
 */
@Data
@Accessors(chain = true)
public class ElectricMeterToIOTDto {
    private String deviceImme;

    private String gatewayDeviceId;
    private String meterAddr;
    private String serialPort;
}
