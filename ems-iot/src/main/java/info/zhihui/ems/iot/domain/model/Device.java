package info.zhihui.ems.iot.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Device {
    /**
     * 主键ID
     */
    private Integer id;
    /**
     * 设备编号
     */
    private String deviceNo;
    /**
     * 串口号（网关下挂设备标识）
     */
    private Integer portNo;
    /**
     * 电表通讯地址（网关下挂设备标识）
     */
    private Integer meterAddress;
    /**
     * 设备密钥
     */
    private String deviceSecret;
    /**
     * Modbus 从站地址
     */
    private int slaveAddress;
    /**
     * 设备产品
     */
    private Product product;
    /**
     * 父设备ID（网关ID）
     */
    private Integer parentId;
    /**
     * 最近在线时间
     */
    private LocalDateTime lastOnlineAt;
}
