package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

/**
 * 送电命令翻译器。
 */
@Component
public class AcrelRecoverTranslator extends AbstractAcrelCommandTranslator {

    private static final byte[] FORCE_CLOSE = new byte[]{0x00, 0x01, 0x00, 0x00};

    public AcrelRecoverTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        super(mappingRegistry);
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.RECOVER;
    }

    @Override
    public ModbusRtuRequest toRequest(DeviceCommand command) {
        ModbusMapping mapping = requireMapping(command);
        int slaveAddress = resolveSlaveAddress(command);
        return buildWrite(mapping, slaveAddress, FORCE_CLOSE);
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseWriteResponse(command, payload);
    }
}
