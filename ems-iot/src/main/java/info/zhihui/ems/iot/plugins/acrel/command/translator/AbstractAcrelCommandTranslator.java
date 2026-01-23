package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslator;
import info.zhihui.ems.iot.plugins.acrel.command.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.constants.AcrelProtocolConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * 安科瑞命令翻译器基础能力。
 */
public abstract class AbstractAcrelCommandTranslator implements DeviceCommandTranslator<ModbusRtuRequest> {

    protected final AcrelModbusMappingRegistry mappingRegistry;

    protected AbstractAcrelCommandTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        this.mappingRegistry = mappingRegistry;
    }

    /**
     * 返回当前翻译器的厂商标识。
     */
    @Override
    public String vendor() {
        return AcrelProtocolConstants.VENDOR;
    }

    @Override
    public Class<ModbusRtuRequest> requestType() {
        return ModbusRtuRequest.class;
    }

    /**
     * 获取命令对应的 Modbus 地址映射。
     */
    protected ModbusMapping requireMapping(DeviceCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command 不能为空");
        }
        ModbusMapping mapping = mappingRegistry.resolve(command.getType());
        if (mapping == null) {
            throw new IllegalStateException("Modbus 地址映射未配置，type=" + command.getType());
        }
        return mapping;
    }

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
        try {
            Object value = parser == null ? data : parser.apply(data);
            return success(command, value, payload);
        } catch (RuntimeException ex) {
            return failure(command, ex.getMessage(), payload);
        }
    }

    /**
     * 将 Modbus 寄存器数据解析为 16 位整型列表。
     */
    protected List<Integer> parseRegisterValues(byte[] data) {
        if (data == null || data.length == 0) {
            return List.of();
        }
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Modbus 寄存器数据长度不正确");
        }
        int size = data.length / 2;
        List<Integer> values = new ArrayList<>(size);
        for (int i = 0; i < data.length; i += 2) {
            int value = (Byte.toUnsignedInt(data[i]) << 8) | Byte.toUnsignedInt(data[i + 1]);
            values.add(value);
        }
        return values;
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
