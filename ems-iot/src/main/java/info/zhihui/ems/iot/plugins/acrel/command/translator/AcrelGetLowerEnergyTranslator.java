package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import org.springframework.stereotype.Component;

/**
 * 读取谷电量命令翻译器。
 */
@Component
public class AcrelGetLowerEnergyTranslator extends AbstractAcrelEnergyTranslator {

    public AcrelGetLowerEnergyTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        super(mappingRegistry);
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_LOWER_ENERGY;
    }
}
