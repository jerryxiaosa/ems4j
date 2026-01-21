package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.command.concrete.SetDatePlanCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.command.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

/**
 * 设置日期方案命令翻译器。
 */
@Component
public class AcrelSetDatePlanTranslator extends AbstractAcrelCommandTranslator {

    public AcrelSetDatePlanTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        super(mappingRegistry);
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_DATE_PLAN;
    }

    @Override
    public ModbusRtuRequest toRequest(DeviceCommand command) {
        SetDatePlanCommand payload = (SetDatePlanCommand) command.getPayload();
        payload.validate();
        ModbusMapping mapping = requireMapping(command);
        throw new IllegalStateException("SET_DATE_PLAN 编码未配置，startRegister=" + mapping.getStartRegister());
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseWriteResponse(command, payload);
    }
}
