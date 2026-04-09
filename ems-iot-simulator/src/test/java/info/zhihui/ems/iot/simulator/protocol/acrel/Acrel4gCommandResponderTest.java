package info.zhihui.ems.iot.simulator.protocol.acrel;

import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelRegisterMappingEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.simulator.protocol.acrel.result.CommandHandleResult;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

class Acrel4gCommandResponderTest {

    private final Acrel4gFrameCodec frameCodec = new Acrel4gFrameCodec();
    private final Acrel4gCommandResponder responder = new Acrel4gCommandResponder(frameCodec);

    @Test
    void handle_whenCutOffCommand_shouldTurnSwitchOffAndReturnWriteAck() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setSwitchStatus("ON");

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildWriteCommand(AcrelRegisterMappingEnum.CONTROL, new byte[]{0x00, 0x01, 0x00, 0x01})));

        Assertions.assertTrue(result.isHandled());
        Assertions.assertEquals("OFF", runtimeState.getSwitchStatus());
        Assertions.assertArrayEquals(buildWriteAck(AcrelRegisterMappingEnum.CONTROL), result.getResponseFrame());
    }

    @Test
    void handle_whenRecoverCommand_shouldTurnSwitchOnAndReturnWriteAck() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setSwitchStatus("OFF");

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildWriteCommand(AcrelRegisterMappingEnum.CONTROL, new byte[]{0x00, 0x01, 0x00, 0x00})));

        Assertions.assertTrue(result.isHandled());
        Assertions.assertEquals("ON", runtimeState.getSwitchStatus());
        Assertions.assertArrayEquals(buildWriteAck(AcrelRegisterMappingEnum.CONTROL), result.getResponseFrame());
    }

    @Test
    void handle_whenReadTotalEnergyCommand_shouldReturnCurrentTotalEnergyAck() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setLastTotalEnergy(new BigDecimal("123.45"));

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildReadCommand(AcrelRegisterMappingEnum.TOTAL_ENERGY)));

        Assertions.assertTrue(result.isHandled());
        Assertions.assertArrayEquals(withCrc(new byte[]{
                0x01,
                (byte) ModbusRtuBuilder.FUNCTION_READ,
                0x04,
                0x00,
                0x00,
                0x30,
                0x39}), result.getResponseFrame());
    }

    @Test
    void handle_whenReadHigherEnergyCommand_shouldReturnCurrentHigherEnergyAck() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setLastHigherEnergy(new BigDecimal("23.45"));

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildReadCommand(AcrelRegisterMappingEnum.HIGHER_ENERGY)));

        Assertions.assertTrue(result.isHandled());
        Assertions.assertArrayEquals(expectedReadAck(2345), result.getResponseFrame());
    }

    @Test
    void handle_whenReadHighEnergyCommand_shouldReturnCurrentHighEnergyAck() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setLastHighEnergy(new BigDecimal("34.56"));

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildReadCommand(AcrelRegisterMappingEnum.HIGH_ENERGY)));

        Assertions.assertTrue(result.isHandled());
        Assertions.assertArrayEquals(expectedReadAck(3456), result.getResponseFrame());
    }

    @Test
    void handle_whenReadLowEnergyCommand_shouldReturnCurrentLowEnergyAck() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setLastLowEnergy(new BigDecimal("45.67"));

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildReadCommand(AcrelRegisterMappingEnum.LOW_ENERGY)));

        Assertions.assertTrue(result.isHandled());
        Assertions.assertArrayEquals(expectedReadAck(4567), result.getResponseFrame());
    }

    @Test
    void handle_whenReadLowerEnergyCommand_shouldReturnCurrentLowerEnergyAck() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setLastLowerEnergy(new BigDecimal("56.78"));

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildReadCommand(AcrelRegisterMappingEnum.LOWER_ENERGY)));

        Assertions.assertTrue(result.isHandled());
        Assertions.assertArrayEquals(expectedReadAck(5678), result.getResponseFrame());
    }

    @Test
    void handle_whenReadDeepLowEnergyCommand_shouldReturnCurrentDeepLowEnergyAck() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setLastDeepLowEnergy(new BigDecimal("67.89"));

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildReadCommand(AcrelRegisterMappingEnum.DEEP_LOW_ENERGY)));

        Assertions.assertTrue(result.isHandled());
        Assertions.assertArrayEquals(expectedReadAck(6789), result.getResponseFrame());
    }

    @Test
    void handle_whenFrameIsNotDownlink_shouldReturnUnhandled() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setSwitchStatus("ON");

        CommandHandleResult result = responder.handle(runtimeState, frameCodec.encode(Acrel4gCommandConstants.HEARTBEAT, null));

        Assertions.assertFalse(result.isHandled());
        Assertions.assertNull(result.getResponseFrame());
        Assertions.assertEquals("ON", runtimeState.getSwitchStatus());
    }

    @Test
    void handle_whenFrameIsInvalid_shouldReturnUnhandled() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setSwitchStatus("ON");
        byte[] modbusFrame = buildReadCommand(AcrelRegisterMappingEnum.TOTAL_ENERGY);
        modbusFrame[modbusFrame.length - 1] = (byte) (modbusFrame[modbusFrame.length - 1] ^ 0x01);

        CommandHandleResult result = responder.handle(runtimeState, buildDownlinkFrame(modbusFrame));

        Assertions.assertFalse(result.isHandled());
        Assertions.assertNull(result.getResponseFrame());
        Assertions.assertEquals("ON", runtimeState.getSwitchStatus());
    }

    @Test
    void handle_whenControlWriteDataIsUnsupported_shouldReturnUnhandledWithoutMutatingState() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setSwitchStatus("ON");

        CommandHandleResult result = responder.handle(runtimeState,
                buildDownlinkFrame(buildWriteCommand(AcrelRegisterMappingEnum.CONTROL, new byte[]{0x00, 0x02, 0x00, 0x01})));

        Assertions.assertFalse(result.isHandled());
        Assertions.assertNull(result.getResponseFrame());
        Assertions.assertNull(result.getCommandName());
        Assertions.assertEquals("ON", runtimeState.getSwitchStatus());
    }

    private byte[] buildDownlinkFrame(byte[] modbusFrame) {
        return frameCodec.encode(Acrel4gCommandConstants.DOWNLINK, modbusFrame);
    }

    private byte[] buildReadCommand(AcrelRegisterMappingEnum mappingEnum) {
        ModbusMapping mapping = mappingEnum.toMapping();
        return ModbusRtuBuilder.build(new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(mapping.getStartRegister())
                .setQuantity(mapping.getQuantity()));
    }

    private byte[] buildWriteCommand(AcrelRegisterMappingEnum mappingEnum, byte[] data) {
        ModbusMapping mapping = mappingEnum.toMapping();
        return ModbusRtuBuilder.build(new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(mapping.getStartRegister())
                .setQuantity(mapping.getQuantity())
                .setData(data));
    }

    private byte[] buildWriteAck(AcrelRegisterMappingEnum mappingEnum) {
        ModbusMapping mapping = mappingEnum.toMapping();
        return withCrc(new byte[]{
                0x01,
                (byte) ModbusRtuBuilder.FUNCTION_WRITE,
                (byte) ((mapping.getStartRegister() >> 8) & 0xFF),
                (byte) (mapping.getStartRegister() & 0xFF),
                (byte) ((mapping.getQuantity() >> 8) & 0xFF),
                (byte) (mapping.getQuantity() & 0xFF)
        });
    }

    private byte[] expectedReadAck(int energyValue) {
        return withCrc(new byte[]{
                0x01,
                (byte) ModbusRtuBuilder.FUNCTION_READ,
                0x04,
                (byte) ((energyValue >> 24) & 0xFF),
                (byte) ((energyValue >> 16) & 0xFF),
                (byte) ((energyValue >> 8) & 0xFF),
                (byte) (energyValue & 0xFF)
        });
    }

    private byte[] withCrc(byte[] body) {
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        frame[body.length] = crc[0];
        frame[body.length + 1] = crc[1];
        return frame;
    }
}
