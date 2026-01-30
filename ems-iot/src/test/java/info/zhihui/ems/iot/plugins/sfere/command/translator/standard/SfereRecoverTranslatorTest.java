package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.RecoverCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SfereRecoverTranslatorTest {

    @Test
    void type_shouldReturnRecover() {
        SfereRecoverTranslator translator = new SfereRecoverTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.RECOVER, translator.type());
    }

    @Test
    void toRequest_ShouldBuildWriteRequest() {
        SfereRecoverTranslator translator = new SfereRecoverTranslator();
        Device device = new Device().setSlaveAddress(9);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.RECOVER)
                .setPayload(new RecoverCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(9, request.getSlaveAddress());
        Assertions.assertEquals(0x10, request.getFunction());
        Assertions.assertEquals(0x0B25, request.getStartRegister());
        Assertions.assertEquals(1, request.getQuantity());
        Assertions.assertArrayEquals(new byte[]{0x00, 0x01}, request.getData());
    }
}
