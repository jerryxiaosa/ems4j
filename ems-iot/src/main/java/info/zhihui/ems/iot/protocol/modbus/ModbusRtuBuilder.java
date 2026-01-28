package info.zhihui.ems.iot.protocol.modbus;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;

import java.util.Arrays;

/**
 * Modbus RTU 报文构建器。
 */
@Slf4j
public final class ModbusRtuBuilder {

    public static final int FUNCTION_READ = 0x03;
    public static final int FUNCTION_WRITE = 0x10;
    private static final int MAX_READ_QUANTITY = 125;
    private static final int MAX_WRITE_QUANTITY = 123;
    private static final int MAX_WRITE_BYTE_COUNT = 255;

    private ModbusRtuBuilder() {
    }

    public static byte[] build(ModbusRtuRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Modbus 请求不能为空");
        }
        int slaveAddress = request.getSlaveAddress();
        if (slaveAddress < 0 || slaveAddress > 0xFF) {
            throw new IllegalArgumentException("非法从站地址: " + slaveAddress);
        }
        int function = request.getFunction();
        if (function == FUNCTION_READ) {
            return buildRead(request);
        }
        if (function == FUNCTION_WRITE) {
            return buildWrite(request);
        }
        throw new IllegalArgumentException("不支持的功能码: " + function);
    }

    private static byte[] buildRead(ModbusRtuRequest request) {
        int start = request.getStartRegister();
        validateRegisterRange(start);
        int quantity = request.getQuantity();
        if (quantity <= 0 || quantity > MAX_READ_QUANTITY) {
            throw new IllegalArgumentException("读取寄存器数量必须在 1~" + MAX_READ_QUANTITY + " 之间");
        }
        byte[] body = new byte[6];
        body[0] = (byte) (request.getSlaveAddress() & 0xFF);
        body[1] = (byte) FUNCTION_READ;
        body[2] = (byte) ((start >> 8) & 0xFF);
        body[3] = (byte) (start & 0xFF);
        body[4] = (byte) ((quantity >> 8) & 0xFF); // 高字节必然为 0
        body[5] = (byte) (quantity & 0xFF);
        return appendCrc(body);
    }

    private static byte[] buildWrite(ModbusRtuRequest request) {
        int start = request.getStartRegister();
        validateRegisterRange(start);
        int quantity = request.getQuantity();
        byte[] data = getBytes(request, quantity);
        byte[] body = new byte[7 + data.length];
        body[0] = (byte) (request.getSlaveAddress() & 0xFF);
        body[1] = (byte) FUNCTION_WRITE;
        body[2] = (byte) ((start >> 8) & 0xFF);
        body[3] = (byte) (start & 0xFF);
        body[4] = (byte) ((quantity >> 8) & 0xFF);
        body[5] = (byte) (quantity & 0xFF);
        body[6] = (byte) data.length;
        if (data.length > 0) {
            System.arraycopy(data, 0, body, 7, data.length);
        }
        return appendCrc(body);
    }

    private static byte[] getBytes(ModbusRtuRequest request, int quantity) {
        byte[] data = request.getData() == null ? new byte[0] : request.getData();
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("写入数据字节数必须为 2 的倍数");
        }
        if (data.length > MAX_WRITE_BYTE_COUNT) {
            throw new IllegalArgumentException("写入数据字节数不能超过 " + MAX_WRITE_BYTE_COUNT);
        }
        if (quantity <= 0 || quantity > MAX_WRITE_QUANTITY) {
            throw new IllegalArgumentException("写入寄存器数量必须在 1~" + MAX_WRITE_QUANTITY + " 之间");
        }
        long expectedBytes = (long) quantity * 2;
        if (expectedBytes != data.length) {
            throw new IllegalArgumentException("写入寄存器数量与数据长度不一致");
        }
        return data;
    }

    private static byte[] appendCrc(byte[] body) {
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        frame[frame.length - 2] = crc[0];
        frame[frame.length - 1] = crc[1];
        return frame;
    }

    private static void validateRegisterRange(int startRegister) {
        if (startRegister < 0 || startRegister > 0xFFFF) {
            throw new IllegalArgumentException("非法寄存器地址: " + startRegister);
        }
    }
}
