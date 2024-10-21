package sandbox.semo.application.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

public class AES256 {

    private static AesBytesEncryptor getEncryptor() {
        return ApplicationContextUtil.getBean(AesBytesEncryptor.class);
    }

    public static String encrypt(String plainText) {
        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = getEncryptor().encrypt(plainBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decrypt(String encryptedText) {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = getEncryptor().decrypt(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}
