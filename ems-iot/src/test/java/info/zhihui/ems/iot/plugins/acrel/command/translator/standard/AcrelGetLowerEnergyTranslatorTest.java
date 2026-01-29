package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.GetLowerEnergyCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGetLowerEnergyTranslatorTest {

    @Test
    void type_shouldReturnGetLowerEnergy() {
        AcrelGetLowerEnergyTranslator translator = new AcrelGetLowerEnergyTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.GET_LOWER_ENERGY, translator.type());
    }

    @Test
    void toRequest_WithValidCommand_ShouldBuildReadRequest() {
        AcrelGetLowerEnergyTranslator translator = new AcrelGetLowerEnergyTranslator();
        Device device = new Device().setSlaveAddress(3);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_LOWER_ENERGY)
                .setPayload(new GetLowerEnergyCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(3, request.getSlaveAddress());
        Assertions.assertEquals(0x03, request.getFunction());
        Assertions.assertEquals(0x0008, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
    }
}
