package info.zhihui.ems.iot.protocol.port;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 协议签名，用于在未知设备/产品时通过帧特征识别厂商/协议。
 */
@Data
@Accessors(chain = true)
public class ProtocolSignature {
    /** 协议/厂商标识，如 ACREL、VENDOR_XYZ */
    private String vendor;

    /** 可选的产品编码/标识 */
    private String productCode;

    /** 接入方式（直连/网关） */
    private DeviceAccessModeEnum accessMode;

    /** 传输协议类型（TCP/MQTT/WS） */
    private TransportProtocolEnum transportType;
}
