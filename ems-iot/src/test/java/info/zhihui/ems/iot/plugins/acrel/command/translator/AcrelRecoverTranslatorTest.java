package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelRecoverTranslatorTest {

    @Test
    void type_shouldReturnRecover() {
        AcrelRecoverTranslator translator = new AcrelRecoverTranslator(new AcrelModbusMappingRegistry());

        Assertions.assertEquals(DeviceCommandTypeEnum.RECOVER, translator.type());
    }

    @Test
    void toRequest_ShouldBuildWriteRequest() {
        AcrelRecoverTranslator translator = new AcrelRecoverTranslator(new AcrelModbusMappingRegistry());
        Device device = new Device().setSlaveAddress(7);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.RECOVER);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(7, request.getSlaveAddress());
        Assertions.assertEquals(0x10, request.getFunction());
        Assertions.assertEquals(0x0057, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
        Assertions.assertArrayEquals(new byte[]{0x00, 0x01, 0x00, 0x00}, request.getData());
    }
}
