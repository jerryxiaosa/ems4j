package info.zhihui.ems.iot.simulator.protocol.acrel;

import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelRegisterMappingEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.simulator.protocol.acrel.result.CommandHandleResult;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;

/**
 * 安科瑞 4G 下行命令最小响应器。
 */
@Slf4j
@Component
public class Acrel4gCommandResponder {

    private static final int MODBUS_MIN_LENGTH = 5;
    private static final byte[] CUT_OFF_COMMAND_DATA = new byte[]{0x00, 0x01, 0x00, 0x01};
    private static final byte[] RECOVER_COMMAND_DATA = new byte[]{0x00, 0x01, 0x00, 0x00};

    private final Acrel4gFrameCodec frameCodec;

    public Acrel4gCommandResponder(Acrel4gFrameCodec frameCodec) {
        this.frameCodec = Objects.requireNonNull(frameCodec, "frameCodec cannot be null");
    }

    /**
     * 处理一帧下行报文，识别支持的 Modbus 命令并返回模拟应答。
     */
    public CommandHandleResult handle(DeviceRuntimeState runtimeState, byte[] frame) {
        if (runtimeState == null || frame == null || frame.length == 0) {
            return unhandledResult();
        }
        byte[] modbusFrame = decodeDownlinkPayload(frame);
        if (!isValidModbusFrame(modbusFrame)) {
            return unhandledResult();
        }

        ParsedModbusCommand parsedModbusCommand = parseModbusCommand(modbusFrame);
        if (parsedModbusCommand == null) {
            return unhandledResult();
        }

        return handleParsedCommand(runtimeState, parsedModbusCommand);
    }

    /**
     * 解码 4G 下行帧，并只提取下行命令对应的 Modbus RTU 负载。
     */
    private byte[] decodeDownlinkPayload(byte[] frame) {
        FrameDecodeResult decodeResult = frameCodec.decode(frame);
        if (decodeResult.reason() != null) {
            return null;
        }
        if (!AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DOWNLINK).equalsIgnoreCase(decodeResult.commandKey())) {
            return null;
        }
        return decodeResult.payload();
    }

    /**
     * 将 Modbus RTU 请求识别为当前模拟器支持的命令类型。
     */
    private ParsedModbusCommand parseModbusCommand(byte[] modbusFrame) {
        if (isWriteCommand(modbusFrame, AcrelRegisterMappingEnum.CONTROL, CUT_OFF_COMMAND_DATA)) {
            return new ParsedModbusCommand(CommandTypeEnum.CUT_OFF, modbusFrame);
        }
        if (isWriteCommand(modbusFrame, AcrelRegisterMappingEnum.CONTROL, RECOVER_COMMAND_DATA)) {
            return new ParsedModbusCommand(CommandTypeEnum.RECOVER, modbusFrame);
        }
        if (isReadCommand(modbusFrame, AcrelRegisterMappingEnum.TOTAL_ENERGY)) {
            return new ParsedModbusCommand(CommandTypeEnum.READ_TOTAL_ENERGY, modbusFrame);
        }
        return null;
    }

    /**
     * 根据已识别的命令类型分发到具体处理逻辑。
     */
    private CommandHandleResult handleParsedCommand(DeviceRuntimeState runtimeState,
                                                    ParsedModbusCommand parsedModbusCommand) {
        return switch (parsedModbusCommand.commandTypeEnum()) {
            case CUT_OFF -> handleCutOff(runtimeState, parsedModbusCommand.modbusFrame());
            case RECOVER -> handleRecover(runtimeState, parsedModbusCommand.modbusFrame());
            case READ_TOTAL_ENERGY -> handleReadTotalEnergy(runtimeState, parsedModbusCommand.modbusFrame());
        };
    }

    /**
     * 处理拉闸命令，更新模拟状态并返回写命令应答。
     */
    private CommandHandleResult handleCutOff(DeviceRuntimeState runtimeState, byte[] modbusFrame) {
        runtimeState.setSwitchStatus("OFF");
        log.info("模拟设备收到拉闸命令 deviceNo={} switchStatus=OFF", resolveDeviceNo(runtimeState));
        return handledResult(buildWriteAck(modbusFrame), CommandTypeEnum.CUT_OFF);
    }

    /**
     * 处理合闸命令，更新模拟状态并返回写命令应答。
     */
    private CommandHandleResult handleRecover(DeviceRuntimeState runtimeState, byte[] modbusFrame) {
        runtimeState.setSwitchStatus("ON");
        log.info("模拟设备收到合闸命令 deviceNo={} switchStatus=ON", resolveDeviceNo(runtimeState));
        return handledResult(buildWriteAck(modbusFrame), CommandTypeEnum.RECOVER);
    }

    /**
     * 处理读总电量命令，按协议整数缩放规则返回当前累计电量。
     */
    private CommandHandleResult handleReadTotalEnergy(DeviceRuntimeState runtimeState, byte[] modbusFrame) {
        log.info("模拟设备收到读总电量命令 deviceNo={} totalEnergy={}",
                resolveDeviceNo(runtimeState),
                runtimeState.getLastTotalEnergy());
        return handledResult(buildReadTotalEnergyAck(runtimeState, modbusFrame), CommandTypeEnum.READ_TOTAL_ENERGY);
    }

    /**
     * 构造已处理结果，携带命令名和应答帧。
     */
    private CommandHandleResult handledResult(byte[] responseFrame, CommandTypeEnum commandTypeEnum) {
        return new CommandHandleResult()
                .setHandled(true)
                .setCommandName(commandTypeEnum.name())
                .setResponseFrame(responseFrame);
    }

    /**
     * 构造未处理结果，交由上层忽略该帧。
     */
    private CommandHandleResult unhandledResult() {
        return new CommandHandleResult()
                .setHandled(false);
    }

    /**
     * 判断请求是否为指定寄存器映射的写命令，并校验写入数据内容。
     */
    private boolean isWriteCommand(byte[] modbusFrame, AcrelRegisterMappingEnum mappingEnum, byte[] expectedData) {
        if (modbusFrame.length != 13) {
            return false;
        }
        if ((modbusFrame[1] & 0xFF) != ModbusRtuBuilder.FUNCTION_WRITE) {
            return false;
        }
        if (!matchesMapping(modbusFrame, mappingEnum)) {
            return false;
        }
        int byteCount = modbusFrame[6] & 0xFF;
        if (byteCount != expectedData.length || modbusFrame.length != 7 + byteCount + 2) {
            return false;
        }
        byte[] actualData = Arrays.copyOfRange(modbusFrame, 7, 7 + byteCount);
        return Arrays.equals(expectedData, actualData);
    }

    /**
     * 判断请求是否为指定寄存器映射的读命令。
     */
    private boolean isReadCommand(byte[] modbusFrame, AcrelRegisterMappingEnum mappingEnum) {
        if (modbusFrame.length != 8) {
            return false;
        }
        if ((modbusFrame[1] & 0xFF) != ModbusRtuBuilder.FUNCTION_READ) {
            return false;
        }
        return matchesMapping(modbusFrame, mappingEnum);
    }

    /**
     * 为写寄存器命令构造标准 Modbus RTU 应答。
     */
    private byte[] buildWriteAck(byte[] modbusFrame) {
        byte[] body = Arrays.copyOf(modbusFrame, 6);
        return appendCrc(body);
    }

    /**
     * 为读总电量命令构造 4 字节整数数据应答。
     */
    private byte[] buildReadTotalEnergyAck(DeviceRuntimeState runtimeState, byte[] modbusFrame) {
        int totalEnergyValue = scaleEnergy(runtimeState.getLastTotalEnergy());
        byte[] body = new byte[]{
                modbusFrame[0],
                (byte) ModbusRtuBuilder.FUNCTION_READ,
                0x04,
                (byte) ((totalEnergyValue >> 24) & 0xFF),
                (byte) ((totalEnergyValue >> 16) & 0xFF),
                (byte) ((totalEnergyValue >> 8) & 0xFF),
                (byte) (totalEnergyValue & 0xFF)
        };
        return appendCrc(body);
    }

    /**
     * 将业务侧总电量按协议要求缩放为保留两位小数的整数值。
     */
    private int scaleEnergy(BigDecimal totalEnergy) {
        BigDecimal safeEnergy = totalEnergy == null ? BigDecimal.ZERO : totalEnergy;
        long energyValue = safeEnergy
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
        if (energyValue < 0 || energyValue > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("总电量超出协议整数范围");
        }
        return (int) energyValue;
    }

    /**
     * 校验 Modbus RTU 最小长度和尾部 CRC 是否正确。
     */
    private boolean isValidModbusFrame(byte[] modbusFrame) {
        if (modbusFrame == null || modbusFrame.length < MODBUS_MIN_LENGTH) {
            return false;
        }
        int dataLength = modbusFrame.length - 2;
        int expected = ModbusCrcUtil.crcInt(Arrays.copyOf(modbusFrame, dataLength));
        int actual = ((modbusFrame[modbusFrame.length - 1] & 0xFF) << 8)
                | (modbusFrame[modbusFrame.length - 2] & 0xFF);
        return expected == actual;
    }

    /**
     * 判断请求中的起始寄存器和数量是否与目标映射一致。
     */
    private boolean matchesMapping(byte[] modbusFrame, AcrelRegisterMappingEnum mappingEnum) {
        ModbusMapping mapping = mappingEnum.toMapping();
        return readUInt16(modbusFrame, 2) == mapping.getStartRegister()
                && readUInt16(modbusFrame, 4) == mapping.getQuantity();
    }

    /**
     * 按大端方式读取两个字节组成的无符号 16 位整数。
     */
    private int readUInt16(byte[] frame, int offset) {
        return ((frame[offset] & 0xFF) << 8) | (frame[offset + 1] & 0xFF);
    }

    /**
     * 为响应体追加 Modbus CRC，返回完整 RTU 帧。
     */
    private byte[] appendCrc(byte[] body) {
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        frame[body.length] = crc[0];
        frame[body.length + 1] = crc[1];
        return frame;
    }

    /**
     * 提取日志使用的设备编号，缺失时统一返回 UNKNOWN。
     */
    private String resolveDeviceNo(DeviceRuntimeState runtimeState) {
        if (runtimeState == null || runtimeState.getDeviceNo() == null || runtimeState.getDeviceNo().isBlank()) {
            return "UNKNOWN";
        }
        return runtimeState.getDeviceNo();
    }

    private enum CommandTypeEnum {
        CUT_OFF,
        RECOVER,
        READ_TOTAL_ENERGY
    }

    private record ParsedModbusCommand(CommandTypeEnum commandTypeEnum, byte[] modbusFrame) {
    }
}
