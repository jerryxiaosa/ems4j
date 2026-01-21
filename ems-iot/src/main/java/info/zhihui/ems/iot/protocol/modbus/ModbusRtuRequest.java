package info.zhihui.ems.iot.protocol.modbus;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Modbus RTU 请求结构。
 */
@Data
@Accessors(chain = true)
public class ModbusRtuRequest {

    /**
     * 从站地址（1 字节）。
     */
    private int slaveAddress;
    /**
     * 功能码（1 字节），如 0x03/0x10。
     */
    private int function;
    /**
     * 起始寄存器地址（2 字节，大端）。
     */
    private int startRegister;
    /**
     * 寄存器数量（2 字节，大端）。
     */
    private int quantity;
    /**
     * 写入数据（写指令使用），按协议填充。
     */
    private byte[] data;
}
