package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.sfere.command.constant.SfereRegisterMappingEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.port.outbound.AbstractEnergyCommandTranslator;
import org.springframework.stereotype.Component;

/**
 * 读取峰电量命令翻译器。
 */
@Component
public class SfereGetHighEnergyTranslator extends AbstractEnergyCommandTranslator {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_HIGH_ENERGY;
    }

    @Override
    public String vendor() {
        return VendorEnum.SFERE.name();
    }

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        return SfereRegisterMappingEnum.HIGH_ENERGY.toMapping();
    }
}
