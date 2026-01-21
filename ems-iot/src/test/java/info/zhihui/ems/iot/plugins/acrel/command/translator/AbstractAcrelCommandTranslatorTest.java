package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.command.modbus.AcrelModbusMappingRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

class AbstractAcrelCommandTranslatorTest {

    @Test
    void resolveSlaveAddress_shouldReturnSlaveAddress() {
        TestTranslator translator = new TestTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setDevice(new Device().setSlaveAddress(7));

        Assertions.assertEquals(7, translator.resolveSlave(command));
    }

    @Test
    void parseWriteResponse_whenSuccess_shouldReturnOk() {
        TestTranslator translator = new TestTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.SET_CT);
        byte[] body = new byte[]{0x01, 0x10, 0x00, 0x38, 0x00, 0x01};
        byte[] frame = withCrc(body);

        DeviceCommandResult result = translator.parseWrite(command, frame);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(DeviceCommandTypeEnum.SET_CT, result.getType());
        Assertions.assertArrayEquals(frame, result.getRawPayload());
    }

    @Test
    void parseWriteResponse_whenException_shouldReturnFailure() {
        TestTranslator translator = new TestTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.SET_CT);
        byte[] body = new byte[]{0x01, (byte) 0x90, 0x02};
        byte[] frame = withCrc(body);

        DeviceCommandResult result = translator.parseWrite(command, frame);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertTrue(result.getErrorMessage().contains("异常响应"));
    }

    @Test
    void parseWriteResponse_whenInvalidCrc_shouldReturnFailure() {
        TestTranslator translator = new TestTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.SET_CT);
        byte[] body = new byte[]{0x01, 0x10, 0x00, 0x38, 0x00, 0x01};
        byte[] frame = withCrc(body);
        frame[frame.length - 1] ^= 0x01;

        DeviceCommandResult result = translator.parseWrite(command, frame);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertTrue(result.getErrorMessage().contains("CRC"));
    }

    @Test
    void parseReadResponse_whenSuccess_shouldReturnParsedValue() {
        TestTranslator translator = new TestTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_CT);
        byte[] body = new byte[]{0x01, 0x03, 0x02, 0x12, 0x34};
        byte[] frame = withCrc(body);

        DeviceCommandResult result = translator.parseRead(command, frame, data -> {
            return (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
        });

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(0x1234, result.getData());
    }

    @Test
    void parseReadResponse_whenLengthMismatch_shouldReturnFailure() {
        TestTranslator translator = new TestTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_CT);
        byte[] body = new byte[]{0x01, 0x03, 0x04, 0x12, 0x34};
        byte[] frame = withCrc(body);

        DeviceCommandResult result = translator.parseRead(command, frame, null);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Modbus 响应数据长度不正确", result.getErrorMessage());
    }

    @Test
    void parseReadResponse_whenParserThrows_shouldReturnFailure() {
        TestTranslator translator = new TestTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_CT);
        byte[] body = new byte[]{0x01, 0x03, 0x01, 0x12};
        byte[] frame = withCrc(body);

        DeviceCommandResult result = translator.parseRead(command, frame, data -> {
            throw new IllegalStateException("boom");
        });

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("boom", result.getErrorMessage());
    }

    private static byte[] withCrc(byte[] body) {
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        frame[body.length] = crc[0];
        frame[body.length + 1] = crc[1];
        return frame;
    }

    private static class TestTranslator extends AbstractAcrelCommandTranslator {

        private TestTranslator(AcrelModbusMappingRegistry mappingRegistry) {
            super(mappingRegistry);
        }

        @Override
        public DeviceCommandTypeEnum type() {
            return DeviceCommandTypeEnum.GET_CT;
        }

        @Override
        public ModbusRtuRequest toRequest(DeviceCommand command) {
            return null;
        }

        @Override
        public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
            return null;
        }

        private ModbusMapping require(DeviceCommand command) {
            return requireMapping(command);
        }

        private int resolveSlave(DeviceCommand command) {
            return resolveSlaveAddress(command);
        }

        private DeviceCommandResult parseWrite(DeviceCommand command, byte[] payload) {
            return parseWriteResponse(command, payload);
        }

        private DeviceCommandResult parseRead(DeviceCommand command, byte[] payload, Function<byte[], Object> parser) {
            return parseReadResponse(command, payload, parser);
        }
    }
}
