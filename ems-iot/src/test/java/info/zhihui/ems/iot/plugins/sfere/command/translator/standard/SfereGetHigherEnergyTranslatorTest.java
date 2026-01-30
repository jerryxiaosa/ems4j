package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.GetHigherEnergyCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SfereGetHigherEnergyTranslatorTest {

    @Test
    void type_shouldReturnGetHigherEnergy() {
        SfereGetHigherEnergyTranslator translator = new SfereGetHigherEnergyTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.GET_HIGHER_ENERGY, translator.type());
    }

    @Test
    void toRequest_WithValidCommand_ShouldBuildReadRequest() {
        SfereGetHigherEnergyTranslator translator = new SfereGetHigherEnergyTranslator();
        Device device = new Device().setSlaveAddress(3);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_HIGHER_ENERGY)
                .setPayload(new GetHigherEnergyCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(3, request.getSlaveAddress());
        Assertions.assertEquals(0x03, request.getFunction());
        Assertions.assertEquals(0x008B, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
    }
}
