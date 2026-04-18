package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gPayloadConstants;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;

import java.util.Arrays;

/**
 * 4G 下发应答适配工具。
 */
public final class Acrel4gDownlinkAckSupport {

    private Acrel4gDownlinkAckSupport() {
    }

    public static byte[] adaptResponse(ModbusRtuRequest request, byte[] payload) {
        if (!isWriteRequest(request) || !isSerialOnlyAck(payload)) {
            return payload;
        }
        return buildWriteAck(request);
    }

    private static boolean isWriteRequest(ModbusRtuRequest request) {
        return request != null && request.getFunction() == ModbusRtuBuilder.FUNCTION_WRITE;
    }

    private static boolean isSerialOnlyAck(byte[] payload) {
        if (payload == null || payload.length != Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH) {
            return false;
        }
        boolean hasValue = false;
        for (byte value : payload) {
            if (value == 0x00) {
                continue;
            }
            if (value < 0x20 || value > 0x7E) {
                return false;
            }
            hasValue = true;
        }
        return hasValue;
    }

    private static byte[] buildWriteAck(ModbusRtuRequest request) {
        byte[] body = new byte[6];
        body[0] = (byte) (request.getSlaveAddress() & 0xFF);
        body[1] = (byte) (request.getFunction() & 0xFF);
        body[2] = (byte) ((request.getStartRegister() >> 8) & 0xFF);
        body[3] = (byte) (request.getStartRegister() & 0xFF);
        body[4] = (byte) ((request.getQuantity() >> 8) & 0xFF);
        body[5] = (byte) (request.getQuantity() & 0xFF);
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        frame[body.length] = crc[0];
        frame[body.length + 1] = crc[1];
        return frame;
    }
}
