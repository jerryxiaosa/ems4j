package info.zhihui.ems.iot.util;

import java.nio.charset.StandardCharsets;

public class HexUtil {
    private static final String DIGITS = "0123456789abcdef";
    private static final char[] HEX_DIGITS = DIGITS.toCharArray();
    private static final byte[] HEX_DIGITS_LOWER = DIGITS.getBytes(StandardCharsets.US_ASCII);

    private HexUtil() {
    }

    public static byte[] hexStringToByteArray(String hexStr) {
        if (hexStr == null) {
            throw new IllegalArgumentException("hexStr is null");
        }
        if (hexStr.isEmpty()) {
            return new byte[0];
        }
        int length = hexStr.length();
        if ((length & 1) != 0) {
            throw new IllegalArgumentException("hexStr length must be even");
        }
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int high = Character.digit(hexStr.charAt(i), 16);
            int low = Character.digit(hexStr.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("hexStr contains non-hex character");
            }
            bytes[i / 2] = (byte) ((high << 4) + low);
        }
        return bytes;
    }

    public static String bytesToHexString(byte[] src) {
        if ((src == null) || (src.length == 0)) {
            throw new IllegalArgumentException("src is empty");
        }
        char[] out = new char[src.length * 2];
        int index = 0;
        for (byte value : src) {
            int v = value & 0xFF;
            out[index++] = HEX_DIGITS[v >>> 4];
            out[index++] = HEX_DIGITS[v & 0x0F];
        }
        return new String(out);
    }

    public static byte[] bytesToHexBytesLower(byte[] src) {
        if (src == null || src.length == 0) {
            return new byte[0];
        }
        byte[] out = new byte[src.length * 2];
        int index = 0;
        for (byte value : src) {
            int v = value & 0xFF;
            out[index++] = HEX_DIGITS_LOWER[v >>> 4];
            out[index++] = HEX_DIGITS_LOWER[v & 0x0F];
        }
        return out;
    }

}
