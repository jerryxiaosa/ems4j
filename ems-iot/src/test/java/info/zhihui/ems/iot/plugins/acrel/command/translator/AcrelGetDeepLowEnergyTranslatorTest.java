package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.command.concrete.GetDeepLowEnergyCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGetDeepLowEnergyTranslatorTest {

    @Test
    void type_shouldReturnGetDeepLowEnergy() {
        AcrelGetDeepLowEnergyTranslator translator = new AcrelGetDeepLowEnergyTranslator(new AcrelModbusMappingRegistry());

        Assertions.assertEquals(DeviceCommandTypeEnum.GET_DEEP_LOW_ENERGY, translator.type());
    }

    @Test
    void toRequest_WithValidCommand_ShouldBuildReadRequest() {
        AcrelGetDeepLowEnergyTranslator translator = new AcrelGetDeepLowEnergyTranslator(new AcrelModbusMappingRegistry());
        Device device = new Device().setSlaveAddress(3);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_DEEP_LOW_ENERGY)
                .setPayload(new GetDeepLowEnergyCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(3, request.getSlaveAddress());
        Assertions.assertEquals(0x03, request.getFunction());
        Assertions.assertEquals(0x5030, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
    }
}
