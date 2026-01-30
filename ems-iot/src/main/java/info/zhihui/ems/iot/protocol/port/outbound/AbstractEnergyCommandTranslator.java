package info.zhihui.ems.iot.protocol.port.outbound;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;

/**
 * 电量读取命令翻译器基础能力。
 */
public abstract class AbstractEnergyCommandTranslator extends AbstractModbusCommandTranslator {

    protected AbstractEnergyCommandTranslator() {
    }

    /**
     * 获取命令对应的 Modbus 地址映射。
     */
    protected abstract ModbusMapping resolveMapping(DeviceCommand command);

    @Override
    protected ModbusRtuRequest buildRequest(DeviceCommand command) {
        ModbusMapping mapping = resolveMapping(command);
        int slaveAddress = resolveSlaveAddress(command);
        return buildRead(mapping, slaveAddress);
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseReadResponse(command, payload, data -> {
            if (data.length < 4) {
                throw new IllegalArgumentException("电量数据长度不足");
            }
            return (Byte.toUnsignedInt(data[0]) << 24)
                    | (Byte.toUnsignedInt(data[1]) << 16)
                    | (Byte.toUnsignedInt(data[2]) << 8)
                    | Byte.toUnsignedInt(data[3]);
        });
    }
}
