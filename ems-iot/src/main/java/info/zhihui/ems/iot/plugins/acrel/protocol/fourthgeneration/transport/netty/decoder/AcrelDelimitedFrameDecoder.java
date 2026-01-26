package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.transport.netty.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;

import static info.zhihui.ems.iot.plugins.acrel.protocol.constants.AcrelProtocolConstants.DELIMITER;
import static info.zhihui.ems.iot.plugins.acrel.protocol.constants.AcrelProtocolConstants.DELIMITER_END;

/**
 * 起止符分隔的报文解码器：默认识别 0x7B7B 开头，0x7D7D 结尾，支持分包缓存。
 */
@AllArgsConstructor
public class AcrelDelimitedFrameDecoder extends ByteToMessageDecoder {

    private final int maxFrameLength;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int start = findStart(in);
        if (start < 0) {
            in.skipBytes(in.readableBytes()); // 丢弃无效前缀
            return;
        }
        if (start > 0) {
            in.skipBytes(start);
        }
        int end = findEnd(in);
        if (end < 0) {
            if (in.readableBytes() > maxFrameLength) {
                in.skipBytes(in.readableBytes()); // 超长重置
            }
            return; // 等待更多数据
        }
        if (end > maxFrameLength) {
            in.skipBytes(in.readableBytes());
            return;
        }
        if (in.readableBytes() < end) {
            return;
        }
        byte[] frame = new byte[end];
        in.readBytes(frame);
        out.add(frame);
    }

    private int findStart(ByteBuf in) {
        for (int i = in.readerIndex(); i < in.writerIndex() - 1; i++) {
            if (in.getByte(i) == DELIMITER && in.getByte(i + 1) == DELIMITER) {
                return i - in.readerIndex();
            }
        }
        return -1;
    }

    private int findEnd(ByteBuf in) {
        for (int i = in.readerIndex() + 2; i < in.writerIndex() - 1; i++) {
            if (in.getByte(i) == DELIMITER_END && in.getByte(i + 1) == DELIMITER_END) {
                return i - in.readerIndex() + 2;
            }
        }
        return -1;
    }
}
