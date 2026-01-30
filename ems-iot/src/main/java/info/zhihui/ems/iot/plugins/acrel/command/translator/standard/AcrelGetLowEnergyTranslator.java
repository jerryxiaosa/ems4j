package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.port.outbound.AbstractEnergyCommandTranslator;
import org.springframework.stereotype.Component;

/**
 * 读取平电量命令翻译器。
 */
@Component
public class AcrelGetLowEnergyTranslator extends AbstractEnergyCommandTranslator {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_LOW_ENERGY;
    }

    @Override
    public String vendor() {
        return VendorEnum.ACREL.name();
    }

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        return AcrelRegisterMappingEnum.LOW_ENERGY.toMapping();
    }
}
