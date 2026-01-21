package info.zhihui.ems.iot.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HexUtilTest {

    @Test
    void hexStringToByteArray_shouldConvertLowercaseAndUppercase() {
        byte[] data = HexUtil.hexStringToByteArray("0a1B");
        Assertions.assertArrayEquals(new byte[]{0x0a, 0x1b}, data);
    }

    @Test
    void hexStringToByteArray_whenEmpty_shouldReturnEmpty() {
        Assertions.assertArrayEquals(new byte[0], HexUtil.hexStringToByteArray(""));
    }

    @Test
    void hexStringToByteArray_whenNull_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HexUtil.hexStringToByteArray(null));
    }

    @Test
    void hexStringToByteArray_whenOddLength_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HexUtil.hexStringToByteArray("ABC"));
    }

    @Test
    void hexStringToByteArray_whenInvalidChar_shouldThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HexUtil.hexStringToByteArray("ZZ"));
    }

    @Test
    void bytesToHexString_whenNull_shouldReturnNull() {
        Assertions.assertNull(HexUtil.bytesToHexString(null));
    }

    @Test
    void bytesToHexString_whenEmpty_shouldReturnNull() {
        Assertions.assertNull(HexUtil.bytesToHexString(new byte[0]));
    }

    @Test
    void bytesToHexString_shouldReturnUppercaseHex() {
        byte[] data = new byte[]{0x0a, 0x1b, (byte) 0xFF};
        Assertions.assertEquals("0A1BFF", HexUtil.bytesToHexString(data));
    }

    @Test
    void bytesToHexString_shouldRoundTrip() {
        byte[] data = new byte[]{0x00, 0x01, 0x10, 0x2f};
        String hex = HexUtil.bytesToHexString(data);
        Assertions.assertArrayEquals(data, HexUtil.hexStringToByteArray(hex));
    }
}
