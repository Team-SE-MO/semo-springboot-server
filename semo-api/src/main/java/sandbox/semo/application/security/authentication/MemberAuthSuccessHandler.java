package sandbox.semo.application.security.authentication;

import static sandbox.semo.application.security.exception.AuthErrorCode.UNAUTHORIZED_USER;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import sandbox.semo.application.security.util.JsonResponseHelper;

@Component
public class MemberAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new BadCredentialsException(UNAUTHORIZED_USER.getMessage()));

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 200);
        responseBody.put("message", "사용자 인증이 완료 되어 로그인 되었습니다.");
        responseBody.put("data", Map.of("ROLE", role));

        JsonResponseHelper.sendJsonSuccessResponse(response, responseBody);
    }

}
