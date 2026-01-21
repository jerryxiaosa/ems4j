package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class AcrelGatewayCryptoServiceTest {

    private static final String SECRET = "1234567890abcdef";

    @Test
    void testEncryptDecrypt_WithValidSecret_ShouldRoundTrip() {
        AcrelGatewayCryptoService service = new AcrelGatewayCryptoService();
        byte[] plain = "hello-gateway".getBytes(StandardCharsets.UTF_8);

        byte[] cipher = service.encrypt(plain, SECRET);
        byte[] decrypted = service.decrypt(cipher, SECRET);

        Assertions.assertArrayEquals(plain, decrypted);
    }

    @Test
    void testEncrypt_WhenSecretNull_ShouldThrow() {
        AcrelGatewayCryptoService service = new AcrelGatewayCryptoService();

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.encrypt(new byte[0], null));
    }

    @Test
    void testEncrypt_WhenSecretLengthInvalid_ShouldThrow() {
        AcrelGatewayCryptoService service = new AcrelGatewayCryptoService();

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.encrypt(new byte[0], "short"));
    }

    @Test
    void testZipUnzip_WhenDataProvided_ShouldRoundTrip() {
        AcrelGatewayCryptoService service = new AcrelGatewayCryptoService();
        byte[] plain = "repeat-repeat-repeat-repeat".getBytes(StandardCharsets.UTF_8);

        byte[] zipped = service.zip(plain);
        byte[] unzipped = service.unzip(zipped);

        Assertions.assertArrayEquals(plain, unzipped);
    }

    @Test
    void testZipUnzip_WhenEmpty_ShouldReturnEmpty() {
        AcrelGatewayCryptoService service = new AcrelGatewayCryptoService();

        Assertions.assertArrayEquals(new byte[0], service.zip(null));
        Assertions.assertArrayEquals(new byte[0], service.unzip(null));
    }

    @Test
    void testUnzip_WhenInvalidData_ShouldThrow() {
        AcrelGatewayCryptoService service = new AcrelGatewayCryptoService();

        Assertions.assertThrows(IllegalStateException.class, () -> service.unzip(new byte[]{0x01, 0x02}));
    }

    @Test
    void testMd5Hex_WhenTextProvided_ShouldReturnDigest() {
        AcrelGatewayCryptoService service = new AcrelGatewayCryptoService();

        Assertions.assertEquals("900150983cd24fb0d6963f7d28e17f72", service.md5Hex("abc"));
        Assertions.assertEquals("", service.md5Hex(null));
    }
}
