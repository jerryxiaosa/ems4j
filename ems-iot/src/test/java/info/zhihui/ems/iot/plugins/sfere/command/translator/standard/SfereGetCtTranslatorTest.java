package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.GetCtCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SfereGetCtTranslatorTest {

    @Test
    void type_shouldReturnGetCt() {
        SfereGetCtTranslator translator = new SfereGetCtTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.GET_CT, translator.type());
    }

    @Test
    void toRequest_WithValidCommand_ShouldBuildReadRequest() {
        SfereGetCtTranslator translator = new SfereGetCtTranslator();
        Device device = new Device().setSlaveAddress(6);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setPayload(new GetCtCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(6, request.getSlaveAddress());
        Assertions.assertEquals(0x03, request.getFunction());
        Assertions.assertEquals(0x080B, request.getStartRegister());
        Assertions.assertEquals(1, request.getQuantity());
    }
}
