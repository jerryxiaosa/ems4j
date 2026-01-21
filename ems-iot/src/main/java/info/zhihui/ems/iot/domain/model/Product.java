package info.zhihui.ems.iot.domain.model;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.common.enums.DeviceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Product {
    private String code;
    private String vendor;
    private DeviceTypeEnum deviceType;
    private Boolean isNb;
    private Boolean hasParent;
    private TransportProtocolEnum protocol;
    private DeviceAccessModeEnum accessMode;
}
