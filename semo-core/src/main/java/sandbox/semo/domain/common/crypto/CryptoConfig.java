package sandbox.semo.domain.common.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

@Configuration
public class CryptoConfig {

    @Value("${encryption.aes.secret}")
    private String symmetricKey;

    @Value("${encryption.aes.salt}")
    private String saltKey;

    @Bean
    public AesBytesEncryptor aesBytesEncryptor() {
        return new AesBytesEncryptor(symmetricKey, saltKey);
    }

}
