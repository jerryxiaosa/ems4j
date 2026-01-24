package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

/**
 * 读取 CT 命令翻译器。
 */
@Component
public class AcrelGetCtTranslator extends AbstractAcrelCommandTranslator {

    public AcrelGetCtTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        super(mappingRegistry);
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_CT;
    }

    @Override
    public ModbusRtuRequest toRequest(DeviceCommand command) {
        ModbusMapping mapping = requireMapping(command);
        int slaveAddress = resolveSlaveAddress(command);
        return buildRead(mapping, slaveAddress);
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseReadResponse(command, payload, data -> {
            if (data.length < 2) {
                throw new IllegalArgumentException("CT 数据长度不足");
            }
            return (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
        });
    }
}
