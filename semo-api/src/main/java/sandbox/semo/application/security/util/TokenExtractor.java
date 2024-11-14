package sandbox.semo.application.security.util;

import static sandbox.semo.application.security.constant.SecurityConstants.JWT_TOKEN_PREFIX;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenExtractor {

    public static String extractToken(String authorization, HttpServletRequest request) {
        if (authorization == null) {
            return extractTokenFromQuery(request);
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

    private static String extractTokenFromQuery(HttpServletRequest request) {
        String tokenParam = request.getParameter("token");
        if (tokenParam == null || tokenParam.isBlank()) {
            log.error(">>> [ ❌ Authorization 헤더 또는 쿼리 파라미터에 토큰이 없습니다 ]");
            throw new BadCredentialsException("잘못된 요청입니다");
        }
        return tokenParam;
    }

}
