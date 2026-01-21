package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 安科瑞网关 AES/ZIP 加解密工具。
 */
@Component
public class AcrelGatewayCryptoService {

    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";
    private static final int MAX_UNZIP_BYTES = 16 * 1024 * 1024;

    public byte[] encrypt(byte[] plain, String secret) {
        return doCipher(plain, secret, Cipher.ENCRYPT_MODE);
    }

    public byte[] decrypt(byte[] cipher, String secret) {
        return doCipher(cipher, secret, Cipher.DECRYPT_MODE);
    }

    public byte[] zip(byte[] plain) {
        if (plain == null || plain.length == 0) {
            return new byte[0];
        }
        Deflater deflater = new Deflater();
        deflater.setInput(plain);
        deflater.finish();
        byte[] buffer = new byte[256];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            output.write(buffer, 0, count);
        }
        deflater.end();
        return output.toByteArray();
    }

    public byte[] unzip(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        byte[] buffer = new byte[256];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int total = 0;
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0) {
                    if (inflater.needsInput()) {
                        break;
                    }
                    if (inflater.needsDictionary()) {
                        throw new IllegalStateException("ZIP 解压失败：缺少字典");
                    }
                    throw new IllegalStateException("ZIP 解压失败：输出为空");
                }
                total += count;
                if (total > MAX_UNZIP_BYTES) {
                    throw new IllegalStateException("ZIP 解压超出最大限制");
                }
                output.write(buffer, 0, count);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("ZIP 解压失败", ex);
        } finally {
            inflater.end();
        }
        return output.toByteArray();
    }

    public String md5Hex(String text) {
        if (text == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("MD5 计算失败", ex);
        }
    }

    private byte[] doCipher(byte[] data, String secret, int mode) {
        if (secret == null) {
            throw new IllegalArgumentException("设备密钥不能为空");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("设备密钥长度必须为 16 字节");
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            cipher.init(mode, keySpec, ivSpec);
            return cipher.doFinal(data == null ? new byte[0] : data);
        } catch (Exception ex) {
            throw new IllegalStateException("AES 加解密失败", ex);
        }
    }
}
