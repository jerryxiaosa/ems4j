package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import org.springframework.stereotype.Component;

/**
 * 读取深谷电量命令翻译器。
 */
@Component
public class AcrelGetDeepLowEnergyTranslator extends AbstractAcrelEnergyTranslator {

    public AcrelGetDeepLowEnergyTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        super(mappingRegistry);
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DEEP_LOW_ENERGY;
    }
}
