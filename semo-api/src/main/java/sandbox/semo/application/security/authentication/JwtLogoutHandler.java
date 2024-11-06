package sandbox.semo.application.security.authentication;

import static sandbox.semo.application.security.util.TokenExtractor.extractToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import sandbox.semo.application.security.util.RedisUtil;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtLogoutHandler implements LogoutHandler {

    private final RedisUtil redisUtil;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {

        String authorization = request.getHeader("Authorization");
        String token = extractToken(authorization);

        try {
            redisUtil.addToBlacklist(token);
        } catch (Exception e) {
            log.error(">>> [ ❌ 로그아웃 처리 중 오류: {} ]", e.getMessage());
        }

    }
}
