package sandbox.semo.security.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import sandbox.semo.security.exception.ErrorCode;
import sandbox.semo.security.util.JsonResponseHelper;

@Log4j2
@Component
public class MemberAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        log.error(">>> [ ❌ 인증 실패: {} ]", exception.getMessage());
        ErrorCode errorCode = getErrorMessage(exception);
        JsonResponseHelper.sendJsonErrorResponse(response, errorCode);
    }

    private ErrorCode getErrorMessage(AuthenticationException exception) {
        if (exception instanceof UsernameNotFoundException
                || exception instanceof BadCredentialsException) {
            return ErrorCode.INVALID_CREDENTIALS;
        }
        return ErrorCode.UNAUTHORIZED_USER;
    }

}
