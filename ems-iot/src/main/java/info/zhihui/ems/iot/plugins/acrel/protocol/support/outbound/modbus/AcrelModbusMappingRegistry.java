package info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus;

import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * 安科瑞 Modbus 地址映射注册表（代码内维护）。
 */
@Component
public class AcrelModbusMappingRegistry {

    private static final int CT_REGISTER = 0x0038;
    private static final int CONTROL_REGISTER = 0x0057;
    private static final int TOTAL_ENERGY_REGISTER = 0x0000;
    private static final int HIGHER_ENERGY_REGISTER = 0x0002;
    private static final int HIGH_ENERGY_REGISTER = 0x0004;
    private static final int LOW_ENERGY_REGISTER = 0x0006;
    private static final int LOWER_ENERGY_REGISTER = 0x0008;
    private static final int DEEP_LOW_ENERGY_REGISTER = 0x5030;

    private final Map<DeviceCommandTypeEnum, ModbusMapping> mappingByType;

    public AcrelModbusMappingRegistry() {
        this.mappingByType = Collections.unmodifiableMap(buildDefaultMapping());
    }

    public ModbusMapping resolve(DeviceCommandTypeEnum type) {
        if (type == null) {
            return null;
        }
        return mappingByType.get(type);
    }

    private Map<DeviceCommandTypeEnum, ModbusMapping> buildDefaultMapping() {
        Map<DeviceCommandTypeEnum, ModbusMapping> map = new EnumMap<>(DeviceCommandTypeEnum.class);
        map.put(DeviceCommandTypeEnum.GET_CT, mapping(CT_REGISTER, 1));
        map.put(DeviceCommandTypeEnum.SET_CT, mapping(CT_REGISTER, 1));
        map.put(DeviceCommandTypeEnum.CUT_OFF, mapping(CONTROL_REGISTER, 2));
        map.put(DeviceCommandTypeEnum.RECOVER, mapping(CONTROL_REGISTER, 2));
        map.put(DeviceCommandTypeEnum.GET_TOTAL_ENERGY, mapping(TOTAL_ENERGY_REGISTER, 2));
        map.put(DeviceCommandTypeEnum.GET_HIGHER_ENERGY, mapping(HIGHER_ENERGY_REGISTER, 2));
        map.put(DeviceCommandTypeEnum.GET_HIGH_ENERGY, mapping(HIGH_ENERGY_REGISTER, 2));
        map.put(DeviceCommandTypeEnum.GET_LOW_ENERGY, mapping(LOW_ENERGY_REGISTER, 2));
        map.put(DeviceCommandTypeEnum.GET_LOWER_ENERGY, mapping(LOWER_ENERGY_REGISTER, 2));
        map.put(DeviceCommandTypeEnum.GET_DEEP_LOW_ENERGY, mapping(DEEP_LOW_ENERGY_REGISTER, 2));
        return map;
    }

    private ModbusMapping mapping(int startRegister, int quantity) {
        return new ModbusMapping()
                .setStartRegister(startRegister)
                .setQuantity(quantity);
    }
}
