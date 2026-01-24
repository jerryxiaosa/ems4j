package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.command.concrete.GetLowEnergyCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGetLowEnergyTranslatorTest {

    @Test
    void type_shouldReturnGetLowEnergy() {
        AcrelGetLowEnergyTranslator translator = new AcrelGetLowEnergyTranslator(new AcrelModbusMappingRegistry());

        Assertions.assertEquals(DeviceCommandTypeEnum.GET_LOW_ENERGY, translator.type());
    }

    @Test
    void toRequest_WithValidCommand_ShouldBuildReadRequest() {
        AcrelGetLowEnergyTranslator translator = new AcrelGetLowEnergyTranslator(new AcrelModbusMappingRegistry());
        Device device = new Device().setSlaveAddress(3);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_LOW_ENERGY)
                .setPayload(new GetLowEnergyCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(3, request.getSlaveAddress());
        Assertions.assertEquals(0x03, request.getFunction());
        Assertions.assertEquals(0x0006, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
    }
}
