package info.zhihui.ems.iot.plugins.acrel.protocol.support;

/**
 * 安科瑞协议报文命令 key 支持。
 */
public final class AcrelPacketKeySupport {

    private AcrelPacketKeySupport() {
    }

    /**
     * 将单字节命令码转换成两位小写十六进制 key。
     */
    public static String commandKey(byte packetCode) {
        return String.format("%02x", Byte.toUnsignedInt(packetCode));
    }
}
