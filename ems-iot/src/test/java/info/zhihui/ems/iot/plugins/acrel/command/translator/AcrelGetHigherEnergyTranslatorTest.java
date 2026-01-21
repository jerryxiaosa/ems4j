package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.command.concrete.GetHigherEnergyCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.command.modbus.AcrelModbusMappingRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGetHigherEnergyTranslatorTest {

    @Test
    void type_shouldReturnGetHigherEnergy() {
        AcrelGetHigherEnergyTranslator translator = new AcrelGetHigherEnergyTranslator(new AcrelModbusMappingRegistry());

        Assertions.assertEquals(DeviceCommandTypeEnum.GET_HIGHER_ENERGY, translator.type());
    }

    @Test
    void toRequest_WithValidCommand_ShouldBuildReadRequest() {
        AcrelGetHigherEnergyTranslator translator = new AcrelGetHigherEnergyTranslator(new AcrelModbusMappingRegistry());
        Device device = new Device().setSlaveAddress(3);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_HIGHER_ENERGY)
                .setPayload(new GetHigherEnergyCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(3, request.getSlaveAddress());
        Assertions.assertEquals(0x03, request.getFunction());
        Assertions.assertEquals(0x0002, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
    }
}
