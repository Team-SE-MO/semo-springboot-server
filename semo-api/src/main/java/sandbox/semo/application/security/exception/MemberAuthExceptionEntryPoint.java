package sandbox.semo.application.security.exception;

import static sandbox.semo.application.security.exception.AuthErrorCode.UNAUTHORIZED_USER;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import sandbox.semo.application.security.util.JsonResponseHelper;

@Log4j2
@Component
public class MemberAuthExceptionEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        log.error(">>> [ ❌ 인증 실패: {} ]", authException.getMessage());
        JsonResponseHelper.sendJsonErrorResponse(response, UNAUTHORIZED_USER);
    }

}
