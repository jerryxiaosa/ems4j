package info.zhihui.ems.iot.simulator.config;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import lombok.Data;

/**
 * 单台模拟电表配置。
 */
@Data
public class SimulatorDeviceProperties {

    /**
     * 系统内已建档的设备编号。
     */
    private String deviceNo;

    /**
     * 厂商标识。
     */
    private VendorEnum vendor;

    /**
     * 产品编码。
     */
    private String productCode;

    /**
     * 接入方式。
     */
    private DeviceAccessModeEnum accessMode;

    /**
     * 电表地址。
     */
    private String meterAddress;

    /**
     * 负载场景类型。
     */
    private ProfileTypeEnum profileType;

    /**
     * 随机种子。
     */
    private Long randomSeed;
}
