package info.zhihui.ems.iot.protocol.modbus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ModbusRtuBuilderTest {

    @Test
    void build_whenRequestNull_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(null));
    }

    @Test
    void build_whenSlaveAddressInvalid_shouldThrow() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x1FF)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0001)
                .setQuantity(1);

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void build_whenFunctionUnsupported_shouldThrow() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(0x01)
                .setStartRegister(0x0001)
                .setQuantity(1);

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildRead_shouldBuildFrameWithCrc() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0010)
                .setQuantity(0x0002);

        byte[] frame = ModbusRtuBuilder.build(request);

        Assertions.assertEquals(8, frame.length);
        Assertions.assertEquals((byte) 0x01, frame[0]);
        Assertions.assertEquals((byte) 0x03, frame[1]);
        Assertions.assertEquals((byte) 0x00, frame[2]);
        Assertions.assertEquals((byte) 0x10, frame[3]);
        Assertions.assertEquals((byte) 0x00, frame[4]);
        Assertions.assertEquals((byte) 0x02, frame[5]);

        byte[] crcInput = Arrays.copyOf(frame, 6);
        byte[] expectedCrc = ModbusCrcUtil.crc(crcInput);
        Assertions.assertEquals(expectedCrc[0], frame[6]);
        Assertions.assertEquals(expectedCrc[1], frame[7]);
    }

    @Test
    void buildRead_whenQuantityZero_shouldThrow() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0001)
                .setQuantity(0);

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildRead_whenQuantityTooLarge_shouldThrow() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0001)
                .setQuantity(126);

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildRead_whenStartRegisterInvalid_shouldThrow() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(-1)
                .setQuantity(1);

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildWrite_shouldBuildFrameWithCrc() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(0x0100)
                .setQuantity(0x0002)
                .setData(new byte[]{0x00, 0x01, 0x00, 0x02});

        byte[] frame = ModbusRtuBuilder.build(request);

        Assertions.assertEquals(13, frame.length);
        Assertions.assertEquals((byte) 0x01, frame[0]);
        Assertions.assertEquals((byte) 0x10, frame[1]);
        Assertions.assertEquals((byte) 0x01, frame[2]);
        Assertions.assertEquals((byte) 0x00, frame[3]);
        Assertions.assertEquals((byte) 0x00, frame[4]);
        Assertions.assertEquals((byte) 0x02, frame[5]);
        Assertions.assertEquals((byte) 0x04, frame[6]);
        Assertions.assertArrayEquals(new byte[]{0x00, 0x01, 0x00, 0x02}, Arrays.copyOfRange(frame, 7, 11));

        byte[] crcInput = Arrays.copyOf(frame, 11);
        byte[] expectedCrc = ModbusCrcUtil.crc(crcInput);
        Assertions.assertEquals(expectedCrc[0], frame[11]);
        Assertions.assertEquals(expectedCrc[1], frame[12]);
    }

    @Test
    void buildWrite_whenQuantityZero_shouldInferQuantity() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(0x0100)
                .setQuantity(0)
                .setData(new byte[]{0x00, 0x01, 0x00, 0x02});

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildWrite_whenDataLengthOdd_shouldThrow() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(0x0100)
                .setQuantity(0)
                .setData(new byte[]{0x00, 0x01, 0x02});

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildWrite_whenQuantityZeroAndDataEmpty_shouldThrow() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(0x0100)
                .setQuantity(0)
                .setData(new byte[0]);

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildWrite_whenQuantityMismatch_shouldThrow() {
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(0x0100)
                .setQuantity(2)
                .setData(new byte[]{0x00, 0x01});

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildWrite_whenQuantityTooLarge_shouldThrow() {
        byte[] data = new byte[248];
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(0x0100)
                .setQuantity(124)
                .setData(data);

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }

    @Test
    void buildWrite_whenDataLengthTooLarge_shouldThrow() {
        byte[] data = new byte[256];
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(0x0100)
                .setQuantity(128)
                .setData(data);

        Assertions.assertThrows(IllegalArgumentException.class, () -> ModbusRtuBuilder.build(request));
    }
}
