package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.constant;

/**
 * 安科瑞网关帧层常量。
 */
public final class AcrelGatewayFrameConstants {

    private AcrelGatewayFrameConstants() {
    }

    /**
     * 网关帧固定帧头。
     */
    public static final short GATEWAY_HEAD = (short) 0x1f1f;

    /**
     * 网关帧头部固定长度：帧头 2 字节 + 命令码 1 字节 + 数据长度 4 字节。
     */
    public static final int FRAME_HEADER_LENGTH = 2 + 1 + 4;

    /**
     * 单帧允许的最大数据区长度，用于解码时做边界保护。
     */
    public static final int MAX_FRAME_DATA_LENGTH = 16 * 1024 * 1024;
}
