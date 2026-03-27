package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.constant;

/**
 * 安科瑞网关命令字常量。
 */
public final class AcrelGatewayCommandConstants {

    private AcrelGatewayCommandConstants() {
    }

    /**
     * 鉴权报文命令字。
     */
    public static final byte AUTH = 0x01;

    /**
     * 心跳报文命令字。
     */
    public static final byte HEARTBEAT = 0x02;

    /**
     * 数据报文命令字。
     */
    public static final byte DATA = 0x03;

    /**
     * 压缩数据报文命令字。
     */
    public static final byte DATA_ZIP = 0x04;

    /**
     * 下行报文命令字。
     */
    public static final byte DOWNLINK = (byte) 0xF1;

    /**
     * 下行应答报文命令字。
     */
    public static final byte DOWNLINK_ACK = (byte) 0xF2;
}
