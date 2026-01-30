package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.CutOffCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SfereCutOffTranslatorTest {

    @Test
    void type_shouldReturnCutOff() {
        SfereCutOffTranslator translator = new SfereCutOffTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.CUT_OFF, translator.type());
    }

    @Test
    void toRequest_ShouldBuildWriteRequest() {
        SfereCutOffTranslator translator = new SfereCutOffTranslator();
        Device device = new Device().setSlaveAddress(8);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.CUT_OFF)
                .setPayload(new CutOffCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(8, request.getSlaveAddress());
        Assertions.assertEquals(0x10, request.getFunction());
        Assertions.assertEquals(0x0B25, request.getStartRegister());
        Assertions.assertEquals(1, request.getQuantity());
        Assertions.assertArrayEquals(new byte[]{0x00, 0x02}, request.getData());
    }
}
