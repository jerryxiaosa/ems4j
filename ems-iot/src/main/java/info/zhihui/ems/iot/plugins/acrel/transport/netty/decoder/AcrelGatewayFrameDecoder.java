package info.zhihui.ems.iot.plugins.acrel.transport.netty.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static info.zhihui.ems.iot.plugins.acrel.protocol.constants.AcrelProtocolConstants.GATEWAY_HEAD;

/**
 * 网关协议解码器：Head=0x1F1F, Type=1B, Length=4B（大端，Data 长度），Data 可为明文或加密。
 */
public class AcrelGatewayFrameDecoder extends ByteToMessageDecoder {

    private static final int HEADER_LENGTH = 2 + 1 + 4;
    private static final int MAX_DATA_LENGTH = 16 * 1024 * 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < HEADER_LENGTH) {
            return;
        }
        int readerIndex = in.readerIndex();
        short head = in.getShort(readerIndex);
        if (head != GATEWAY_HEAD) {
            in.skipBytes(1);
            return;
        }
        int length = in.getInt(readerIndex + 3); // after head(2) + type(1)
        if (length < 0 || length > MAX_DATA_LENGTH) {
            in.skipBytes(in.readableBytes());
            ctx.close();
            return;
        }
        long frameLength = HEADER_LENGTH + (long) length;

        if (in.readableBytes() < frameLength) {
            return;
        }
        byte[] frame = new byte[(int) frameLength];
        in.readBytes(frame);
        out.add(frame);
    }
}
