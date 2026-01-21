package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet;

/**
 * 4G 电表命令字定义。
 */
public final class Acrel4gPacketCode {

    private Acrel4gPacketCode() {
    }

    public static String commandKey(byte code) {
        return String.format("%02x", Byte.toUnsignedInt(code));
    }

    public static final byte REGISTER = (byte) 0x84;
    public static final byte TIME_SYNC = (byte) 0x93;
    public static final byte DATA_UPLOAD = (byte) 0x91;
    public static final byte DOWNLINK = (byte) 0x90;
    public static final byte HEARTBEAT = (byte) 0x94;
}
