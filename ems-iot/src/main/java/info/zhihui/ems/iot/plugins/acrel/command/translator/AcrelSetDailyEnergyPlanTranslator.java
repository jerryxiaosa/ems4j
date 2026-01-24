package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.command.concrete.SetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

/**
 * 设置每日电量方案命令翻译器。
 */
@Component
public class AcrelSetDailyEnergyPlanTranslator extends AbstractAcrelCommandTranslator {

    public AcrelSetDailyEnergyPlanTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        super(mappingRegistry);
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN;
    }

    @Override
    public ModbusRtuRequest toRequest(DeviceCommand command) {
        SetDailyEnergyPlanCommand payload = (SetDailyEnergyPlanCommand) command.getPayload();
        payload.validate();
        ModbusMapping mapping = requireMapping(command);
        throw new IllegalStateException("SET_DAILY_ENERGY_PLAN 编码未配置，startRegister=" + mapping.getStartRegister());
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseWriteResponse(command, payload);
    }
}
