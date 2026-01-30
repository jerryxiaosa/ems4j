package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.sfere.command.constant.SfereRegisterMappingEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.port.outbound.AbstractEnergyCommandTranslator;
import org.springframework.stereotype.Component;

/**
 * 读取深谷电量命令翻译器。
 */
@Component
public class SfereGetDeepLowEnergyTranslator extends AbstractEnergyCommandTranslator {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DEEP_LOW_ENERGY;
    }

    @Override
    public String vendor() {
        return VendorEnum.SFERE.name();
    }

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        return SfereRegisterMappingEnum.DEEP_LOW_ENERGY.toMapping();
    }
}
