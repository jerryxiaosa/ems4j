package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelCutOffTranslatorTest {

    @Test
    void type_shouldReturnCutOff() {
        AcrelCutOffTranslator translator = new AcrelCutOffTranslator(new AcrelModbusMappingRegistry());

        Assertions.assertEquals(DeviceCommandTypeEnum.CUT_OFF, translator.type());
    }

    @Test
    void toRequest_ShouldBuildWriteRequest() {
        AcrelCutOffTranslator translator = new AcrelCutOffTranslator(new AcrelModbusMappingRegistry());
        Device device = new Device().setSlaveAddress(5);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.CUT_OFF);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(5, request.getSlaveAddress());
        Assertions.assertEquals(0x10, request.getFunction());
        Assertions.assertEquals(0x0057, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
        Assertions.assertArrayEquals(new byte[]{0x00, 0x01, 0x00, 0x01}, request.getData());
    }
}
