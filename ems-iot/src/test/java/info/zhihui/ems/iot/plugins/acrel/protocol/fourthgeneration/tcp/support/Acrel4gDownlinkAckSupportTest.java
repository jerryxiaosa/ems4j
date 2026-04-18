package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gPayloadConstants;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Acrel4gDownlinkAckSupportTest {

    @Test
    void adaptResponse_whenWriteCommandWithSerialOnlyAck_shouldBuildStandardWriteAck() {
        byte[] payload = new byte[Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH];
        byte[] serialBytes = "25062605270193".getBytes();
        System.arraycopy(serialBytes, 0, payload, 0, serialBytes.length);
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(1)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(0x0028)
                .setQuantity(6);

        byte[] response = Acrel4gDownlinkAckSupport.adaptResponse(request, payload);

        Assertions.assertArrayEquals(new byte[]{0x01, 0x10, 0x00, 0x28, 0x00, 0x06, (byte) 0xC0, 0x03}, response);
    }

    @Test
    void adaptResponse_whenReadCommand_shouldKeepOriginalPayload() {
        byte[] payload = new byte[Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH];
        byte[] serialBytes = "25062605270193".getBytes();
        System.arraycopy(serialBytes, 0, payload, 0, serialBytes.length);
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(1)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0028)
                .setQuantity(6);

        byte[] response = Acrel4gDownlinkAckSupport.adaptResponse(request, payload);

        Assertions.assertSame(payload, response);
    }
}
