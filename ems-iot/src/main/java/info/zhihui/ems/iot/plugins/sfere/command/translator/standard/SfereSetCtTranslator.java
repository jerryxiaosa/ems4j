package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.SetCtCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.sfere.command.constant.SfereRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

/**
 * 斯菲尔设置 CT 命令翻译器。
 */
@Component
public class SfereSetCtTranslator extends AbstractSfereCommandTranslator {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_CT;
    }

    @Override
    protected ModbusRtuRequest buildRequest(DeviceCommand command) {
        SetCtCommand payload = (SetCtCommand) command.getPayload();
        ModbusMapping mapping = resolveMapping();
        int ct = payload.getCt();
        // 按寄存器高字节在前的顺序写入 16 位 CT 值
        byte[] data = new byte[]{(byte) ((ct >> 8) & 0xFF), (byte) (ct & 0xFF)};
        int slaveAddress = resolveSlaveAddress(command);

        return buildWrite(mapping, slaveAddress, data);
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseWriteResponse(command, payload);
    }

    private ModbusMapping resolveMapping() {
        return SfereRegisterMappingEnum.CT.toMapping();
    }
}
