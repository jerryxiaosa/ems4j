package info.zhihui.ems.iot.plugins.acrel.command.modbus;

import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelModbusMappingRegistryTest {

    @Test
    void resolve_whenTypeNull_shouldReturnNull() {
        AcrelModbusMappingRegistry registry = new AcrelModbusMappingRegistry();

        Assertions.assertNull(registry.resolve(null));
    }

    @Test
    void resolve_whenDefaultMapping_shouldReturnExpectedRegister() {
        AcrelModbusMappingRegistry registry = new AcrelModbusMappingRegistry();

        ModbusMapping ct = registry.resolve(DeviceCommandTypeEnum.GET_CT);
        Assertions.assertNotNull(ct);
        Assertions.assertEquals(0x0038, ct.getStartRegister());
        Assertions.assertEquals(1, ct.getQuantity());

        ModbusMapping control = registry.resolve(DeviceCommandTypeEnum.CUT_OFF);
        Assertions.assertNotNull(control);
        Assertions.assertEquals(0x0057, control.getStartRegister());
        Assertions.assertEquals(2, control.getQuantity());

        ModbusMapping total = registry.resolve(DeviceCommandTypeEnum.GET_TOTAL_ENERGY);
        Assertions.assertNotNull(total);
        Assertions.assertEquals(0x0000, total.getStartRegister());
        Assertions.assertEquals(2, total.getQuantity());

        ModbusMapping deepLow = registry.resolve(DeviceCommandTypeEnum.GET_DEEP_LOW_ENERGY);
        Assertions.assertNotNull(deepLow);
        Assertions.assertEquals(0x5030, deepLow.getStartRegister());
        Assertions.assertEquals(2, deepLow.getQuantity());
    }
}
