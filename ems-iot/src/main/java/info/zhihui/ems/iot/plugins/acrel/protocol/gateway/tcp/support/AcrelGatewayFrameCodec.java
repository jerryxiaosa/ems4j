package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.protocol.constant.AcrelProtocolConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.transport.netty.decoder.AcrelGatewayFrameDecoder;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.protocol.decode.ProtocolDecodeErrorEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 网关帧编解码器（1F1F + Type + Length + Data）。
 */
@Component
public class AcrelGatewayFrameCodec {

    /**
     * 解码由 {@link AcrelGatewayFrameDecoder}
     * 切分后的完整帧，不在此处重复校验帧头与长度边界。
     */
    public FrameDecodeResult decode(byte[] frame) {
        if (frame == null || frame.length < 7) {
            return new FrameDecodeResult(null, new byte[0], ProtocolDecodeErrorEnum.FRAME_TOO_SHORT);
        }
        byte type = frame[2];
        String commandKey = GatewayPacketCode.commandKey(type);
        byte[] data = frame.length == 7 ? new byte[0] : Arrays.copyOfRange(frame, 7, frame.length);
        return new FrameDecodeResult(commandKey, data, null);
    }

    public byte[] encode(byte type, byte[] data) {
        byte[] payload = data == null ? new byte[0] : data;
        int length = payload.length;
        byte[] frame = new byte[7 + length];
        frame[0] = (byte) ((AcrelProtocolConstants.GATEWAY_HEAD >> 8) & 0xFF);
        frame[1] = (byte) (AcrelProtocolConstants.GATEWAY_HEAD & 0xFF);
        frame[2] = type;
        frame[3] = (byte) ((length >> 24) & 0xFF);
        frame[4] = (byte) ((length >> 16) & 0xFF);
        frame[5] = (byte) ((length >> 8) & 0xFF);
        frame[6] = (byte) (length & 0xFF);
        if (length > 0) {
            System.arraycopy(payload, 0, frame, 7, length);
        }
        return frame;
    }

}
