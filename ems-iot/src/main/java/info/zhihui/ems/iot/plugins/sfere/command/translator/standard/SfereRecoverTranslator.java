package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.sfere.command.constant.SfereRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

/**
 * 斯菲尔合闸命令翻译器。
 */
@Component
public class SfereRecoverTranslator extends AbstractSfereCommandTranslator {

    private static final byte[] RECOVER_COMMAND = new byte[]{0x00, 0x01};

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.RECOVER;
    }

    @Override
    protected ModbusRtuRequest buildRequest(DeviceCommand command) {
        ModbusMapping mapping = resolveMapping();
        int slaveAddress = resolveSlaveAddress(command);
        return buildWrite(mapping, slaveAddress, RECOVER_COMMAND);
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseWriteResponse(command, payload);
    }

    private ModbusMapping resolveMapping() {
        return SfereRegisterMappingEnum.CONTROL.toMapping();
    }
}
