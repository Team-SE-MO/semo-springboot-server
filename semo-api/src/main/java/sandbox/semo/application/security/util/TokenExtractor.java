package sandbox.semo.application.security.util;

import static sandbox.semo.application.security.constant.SecurityConstants.JWT_TOKEN_PREFIX;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenExtractor {
    public static String extractToken(String authorization) {
        if (authorization == null) {
            log.error(">>> [ ❌ Authorization 헤더가 없습니다 ]");
            throw new BadCredentialsException("잘못된 요청입니다");
        }

        if (!authorization.startsWith(JWT_TOKEN_PREFIX)) {
            log.error(">>> [ ❌ 잘못된 토큰 형식입니다: Bearer 형식이 아님 ]");
            throw new BadCredentialsException("잘못된 토큰 형식입니다");
        }

        String token = authorization.substring(JWT_TOKEN_PREFIX.length());
        if (token.isBlank()) {
            log.error(">>> [ ❌ 토큰이 비어있습니다 ]");
            throw new BadCredentialsException("토큰이 비어있습니다");
        }

        return token;
    }
}
