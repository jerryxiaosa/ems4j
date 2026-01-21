package info.zhihui.ems.iot.protocol.modbus;

/**
 * Modbus CRC16 校验工具，小端存储。
 */
public final class ModbusCrcUtil {

    private ModbusCrcUtil() {
    }

    /**
     * 计算 Modbus CRC16，返回小端字节序（低位在前）。
     *
     * @param data 参与校验的数据
     * @return 两字节 CRC
     */
    public static byte[] crc(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc ^= (b & 0xFF);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc >>>= 1;
                }
            }
        }
        byte low = (byte) (crc & 0xFF);
        byte high = (byte) ((crc >> 8) & 0xFF);
        return new byte[]{low, high};
    }

    /**
     * 返回 CRC16 整型值（高字节在前），便于比较/日志输出。
     *
     * @param data 参与校验的数据
     * @return CRC16 整型值
     */
    public static int crcInt(byte[] data) {
        byte[] bytes = crc(data);
        return ((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF);
    }
}
