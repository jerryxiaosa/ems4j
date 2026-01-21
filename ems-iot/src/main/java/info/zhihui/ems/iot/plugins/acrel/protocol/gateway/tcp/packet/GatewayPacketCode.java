package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet;

/**
 * 网关命令字常量。
 */
public final class GatewayPacketCode {

    private GatewayPacketCode() {
    }

    public static String commandKey(byte code) {
        return String.format("%02x", Byte.toUnsignedInt(code));
    }

    public static final byte AUTH = 0x01;
    public static final byte HEARTBEAT = 0x02;
    public static final byte DATA = 0x03;
    public static final byte DATA_ZIP = 0x04;
    public static final byte DOWNLINK = (byte) 0xF1;
    public static final byte DOWNLINK_ACK = (byte) 0xF2;
}
