package info.zhihui.ems.iot.protocol.modbus;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Modbus 地址映射占位。
 */
@Data
@Accessors(chain = true)
public class ModbusMapping {

    /**
     * Modbus 起始寄存器地址。
     */
    private int startRegister;

    /**
     * 寄存器数量（连续区间）。
     */
    private int quantity;
}
