package info.zhihui.ems.iot.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.DeviceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductEnum implements CodeEnum<String> {

    ACREL_GATEWAY(
            "ACREL_GATEWAY",
            "安科瑞网关",
            VendorEnum.ACREL.name(),
            DeviceTypeEnum.GATEWAY,
            TransportProtocolEnum.TCP,
            DeviceAccessModeEnum.GATEWAY,
            false,
            false
    ),
    ACREL_DDSY_1352(
            "ACREL_DDSY_1352",
            "安科瑞单相预付费电表",
            VendorEnum.ACREL.name(),
            DeviceTypeEnum.ELECTRIC,
            TransportProtocolEnum.TCP,
            DeviceAccessModeEnum.GATEWAY,
            false,
            true
    ),
    ACREL_DTSY_1352(
            "ACREL_DTSY_1352",
            "安科瑞三相预付费电表",
            VendorEnum.ACREL.name(),
            DeviceTypeEnum.ELECTRIC,
            TransportProtocolEnum.TCP,
            DeviceAccessModeEnum.GATEWAY,
            false,
            true
    ),
    ACREL_DTSY_1352_4G(
            "ACREL_DTSY_1352_4G",
            "安科瑞三相预付费电表4G",
            VendorEnum.ACREL.name(),
            DeviceTypeEnum.ELECTRIC,
            TransportProtocolEnum.TCP,
            DeviceAccessModeEnum.DIRECT,
            false,
            false
    ),
    ACREL_AWT100_4G_MQTT(
            "ACREL_AWT100_4G_MQTT",
            "安科瑞AWT100_4G_MQTT",
            VendorEnum.ACREL.name(),
            DeviceTypeEnum.ELECTRIC,
            TransportProtocolEnum.MQTT,
            DeviceAccessModeEnum.DIRECT,
            false,
            false
    ),
    ACREL_ADF_400L_MQTT(
            "ACREL_ADF_400L_MQTT",
            "安科瑞ADF_400L_MQTT",
            VendorEnum.ACREL.name(),
            DeviceTypeEnum.ELECTRIC,
            TransportProtocolEnum.MQTT,
            DeviceAccessModeEnum.DIRECT,
            false,
            false
    ),
    ;

    private final String code;
    private final String name;
    private final String vendor;
    private final DeviceTypeEnum deviceType;
    private final TransportProtocolEnum protocol;
    private final DeviceAccessModeEnum accessMode;
    private final boolean isNb;
    private final boolean hasParent;

}
