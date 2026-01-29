package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.GetHighEnergyCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGetHighEnergyTranslatorTest {

    @Test
    void type_shouldReturnGetHighEnergy() {
        AcrelGetHighEnergyTranslator translator = new AcrelGetHighEnergyTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.GET_HIGH_ENERGY, translator.type());
    }

    @Test
    void toRequest_WithValidCommand_ShouldBuildReadRequest() {
        AcrelGetHighEnergyTranslator translator = new AcrelGetHighEnergyTranslator();
        Device device = new Device().setSlaveAddress(3);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_HIGH_ENERGY)
                .setPayload(new GetHighEnergyCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(3, request.getSlaveAddress());
        Assertions.assertEquals(0x03, request.getFunction());
        Assertions.assertEquals(0x0004, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
    }
}
