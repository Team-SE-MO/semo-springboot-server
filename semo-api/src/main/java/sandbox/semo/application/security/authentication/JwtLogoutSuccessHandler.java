package sandbox.semo.application.security.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import sandbox.semo.application.security.util.JsonResponseHelper;

@Component
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 200);
        responseBody.put("message", "성공적으로 로그아웃 되었습니다.");

        JsonResponseHelper.sendJsonSuccessResponse(response, responseBody);
    }
}
