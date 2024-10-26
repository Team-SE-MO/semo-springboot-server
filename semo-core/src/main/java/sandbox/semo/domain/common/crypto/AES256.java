package sandbox.semo.domain.common.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AES256 {

    private final AesBytesEncryptor encryptor;

    public String encrypt(String plainText) {
        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = encryptor.encrypt(plainBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedText) {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = encryptor.decrypt(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}
