package info.zhihui.ems.iot.protocol.port.outbound;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Modbus 命令翻译器基础能力。
 */
@Slf4j
public abstract class AbstractModbusCommandTranslator implements DeviceCommandTranslator<ModbusRtuRequest> {

    protected AbstractModbusCommandTranslator() {
    }

    /**
     * 协议请求类型（用于运行期校验）。
     *
     * @return 协议请求类型
     */
    @Override
    public Class<ModbusRtuRequest> requestType() {
        return ModbusRtuRequest.class;
    }

    /**
     * 将领域命令转换为协议命令。
     */
    @Override
    public final ModbusRtuRequest toRequest(DeviceCommand command) {
        if (command == null || command.getPayload() == null) {
            throw new IllegalArgumentException("命令或命令数据不能为空");
        }

        command.getPayload().validate();
        return buildRequest(command);
    }

    /**
     * 构建 Modbus 请求。
     */
    protected abstract ModbusRtuRequest buildRequest(DeviceCommand command);

    /**
     * 获取命令对应的 Modbus 地址映射。
     */
    protected abstract ModbusMapping resolveMapping(DeviceCommand command);

    /**
     * 获取设备从站地址。
     */
    protected int resolveSlaveAddress(DeviceCommand command) {
        return command.getDevice().getSlaveAddress();
    }

    /**
     * 构造读寄存器请求。
     */
    protected ModbusRtuRequest buildRead(ModbusMapping mapping, int slaveAddress) {
        return new ModbusRtuRequest()
                .setSlaveAddress(slaveAddress)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(mapping.getStartRegister())
                .setQuantity(mapping.getQuantity());
    }

    /**
     * 构造写寄存器请求。
     */
    protected ModbusRtuRequest buildWrite(ModbusMapping mapping, int slaveAddress, byte[] data) {
        return new ModbusRtuRequest()
                .setSlaveAddress(slaveAddress)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(mapping.getStartRegister())
                .setQuantity(mapping.getQuantity())
                .setData(data);
    }

    /**
     * 解析写命令的 Modbus 响应。
     */
    protected DeviceCommandResult parseWriteResponse(DeviceCommand command, byte[] payload) {
        if (!isValidFrame(payload)) {
            return failure(command, "Modbus 响应格式不正确或 CRC 校验失败", payload);
        }
        log.debug("写命令 响应数据：{}", HexUtil.bytesToHexString(payload));

        byte function = payload[1];
        if ((function & 0x80) != 0) {
            return failure(command, buildExceptionMessage(function, payload[2]), payload);
        }
        if (function != ModbusRtuBuilder.FUNCTION_WRITE) {
            return failure(command, "Modbus 响应功能码不匹配", payload);
        }
        return success(command, null, payload);
    }

    /**
     * 解析读命令的 Modbus 响应，并转换为业务数据。
     */
    protected DeviceCommandResult parseReadResponse(DeviceCommand command, byte[] payload, Function<byte[], Object> parser) {
        if (!isValidFrame(payload)) {
            return failure(command, "Modbus 响应格式不正确或 CRC 校验失败", payload);
        }
        log.debug("读命令 响应数据：{}", HexUtil.bytesToHexString(payload));

        byte function = payload[1];
        if ((function & 0x80) != 0) {
            return failure(command, buildExceptionMessage(function, payload[2]), payload);
        }
        if (function != ModbusRtuBuilder.FUNCTION_READ) {
            return failure(command, "Modbus 响应功能码不匹配", payload);
        }
        int byteCount = Byte.toUnsignedInt(payload[2]);
        int dataStart = 3;
        int dataEnd = dataStart + byteCount;
        if (payload.length < dataEnd + 2) {
            return failure(command, "Modbus 响应数据长度不正确", payload);
        }
        byte[] data = Arrays.copyOfRange(payload, dataStart, dataEnd);
        log.debug("Modbus 响应数据：{}", HexUtil.bytesToHexString(data));

        try {
            Object value = parser == null ? data : parser.apply(data);
            return success(command, value, payload);
        } catch (RuntimeException ex) {
            return failure(command, ex.getMessage(), payload);
        }
    }

    /**
     * 校验 Modbus RTU 响应的 CRC。
     */
    private boolean isValidFrame(byte[] payload) {
        if (payload == null || payload.length < 5) {
            return false;
        }
        int dataLen = payload.length - 2;
        int expected = ModbusCrcUtil.crcInt(Arrays.copyOf(payload, dataLen));
        int actual = ((payload[payload.length - 1] & 0xFF) << 8) | (payload[payload.length - 2] & 0xFF);
        return expected == actual;
    }

    /**
     * 生成 Modbus 异常响应提示信息。
     */
    private String buildExceptionMessage(byte function, byte errorCode) {
        int functionCode = function & 0xFF;
        int code = errorCode & 0xFF;
        return "Modbus 异常响应，function=0x" + toHex(functionCode) + ", error=0x" + toHex(code);
    }

    /**
     * 将数值格式化为 2 位 16 进制。
     */
    private String toHex(int value) {
        return String.format("%02X", value);
    }

    /**
     * 构造成功结果。
     */
    private DeviceCommandResult success(DeviceCommand command, Object data, byte[] payload) {
        log.debug("Modbus 响应成功，command={}, data={}", command, data);
        return new DeviceCommandResult()
                .setType(command.getType())
                .setSuccess(true)
                .setData(data)
                .setRawPayload(payload);
    }

    /**
     * 构造失败结果。
     */
    private DeviceCommandResult failure(DeviceCommand command, String message, byte[] payload) {
        return new DeviceCommandResult()
                .setType(command.getType())
                .setSuccess(false)
                .setErrorMessage(message)
                .setRawPayload(payload);
    }
}
