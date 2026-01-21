package info.zhihui.ems.iot.plugins.acrel.constants;

/**
 * 安科瑞协议帧常量，供探测器与解码器复用。
 */
public final class AcrelProtocolConstants {

    private AcrelProtocolConstants() {
    }

    public static final String VENDOR = "ACREL";

    public static final byte DELIMITER = 0x7b; // {{
    public static final byte DELIMITER_END = 0x7d; // }}
    public static final short GATEWAY_HEAD = (short) 0x1f1f;
}
