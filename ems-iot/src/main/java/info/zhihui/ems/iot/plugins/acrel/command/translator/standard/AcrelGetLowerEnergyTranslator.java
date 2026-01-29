package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import org.springframework.stereotype.Component;

/**
 * 读取谷电量命令翻译器。
 */
@Component
public class AcrelGetLowerEnergyTranslator extends AbstractAcrelEnergyTranslator {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_LOWER_ENERGY;
    }

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        return AcrelRegisterMappingEnum.LOWER_ENERGY.toMapping();
    }
}
