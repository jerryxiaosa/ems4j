package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.transport.netty.decoder.AcrelDelimitedFrameDecoder;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.protocol.decode.ProtocolDecodeErrorEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.constants.AcrelProtocolConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 4G 表协议帧编解码器：封装起止符与 CRC。
 */
@Slf4j
@Component
public class Acrel4gFrameCodec {

    /**
     * 解码由 {@link AcrelDelimitedFrameDecoder}
     * 切分后的完整帧，不在此处重复校验起止符。
     */
    public FrameDecodeResult decode(byte[] frame) {
        // 最短帧：7B7B + cmd(1) + CRC(2) + 7D7D = 7 字节
        if (frame == null || frame.length < 7) {
            log.error("帧格式错误：小于最小长度");
            return new FrameDecodeResult(null, new byte[0], ProtocolDecodeErrorEnum.FRAME_TOO_SHORT);
        }
        byte command = frame[2];
        String commandKey = Acrel4gPacketCode.commandKey(command);
        int crcIdx = frame.length - 4;

        // 取出消息体（不含起止符、命令字、CRC）：
        // frame = 7B7B + command(1B) + payload(NB) + crc(2B) + 7D7D
        byte[] payload = crcIdx > 3 ? Arrays.copyOfRange(frame, 3, crcIdx) : new byte[0];

        // 取出帧内携带的 CRC16（小端：低字节在前，高字节在后），并组装成 int 便于对比。
        int crcLow = frame[crcIdx] & 0xFF;
        int crcHigh = frame[crcIdx + 1] & 0xFF;
        int crc = (crcHigh << 8) | crcLow;

        // CRC 校验范围为：命令字(1B) + 消息体(payload)。
        // 协议使用 Modbus CRC16，且在帧中按小端存储（低字节在前，高字节在后）。
        byte[] crcTarget = new byte[1 + payload.length];
        crcTarget[0] = command;
        System.arraycopy(payload, 0, crcTarget, 1, payload.length);
        if (ModbusCrcUtil.crcInt(crcTarget) != crc) {
            log.warn("CRC 校验失败");
            return new FrameDecodeResult(commandKey, payload, ProtocolDecodeErrorEnum.CRC_INVALID);
        }

        return new FrameDecodeResult(commandKey, payload, null);
    }

    public byte[] encode(byte command, byte[] body) {
        byte[] payload = body == null ? new byte[0] : body;
        byte[] crc = ModbusCrcUtil.crc(combine(new byte[]{command}, payload));
        byte[] frame = new byte[2 + 1 + payload.length + 2 + 2];
        int idx = 0;
        frame[idx++] = AcrelProtocolConstants.DELIMITER;
        frame[idx++] = AcrelProtocolConstants.DELIMITER;
        frame[idx++] = command;
        System.arraycopy(payload, 0, frame, idx, payload.length);
        idx += payload.length;
        frame[idx++] = crc[0];
        frame[idx++] = crc[1];
        frame[idx++] = AcrelProtocolConstants.DELIMITER_END;
        frame[idx] = AcrelProtocolConstants.DELIMITER_END;
        return frame;
    }

    public byte[] encodeAck(byte command) {
        return encode(command, new byte[0]);
    }

    private byte[] combine(byte[] a, byte[] b) {
        byte[] res = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

}
