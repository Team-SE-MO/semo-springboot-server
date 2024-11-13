package sandbox.semo.application.security.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sandbox.semo.application.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS(BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_USER(UNAUTHORIZED, "인증 되지 않은 사용자 입니다. 다시 한번 확인해 주세요."),
    TOKEN_EXPIRED(UNAUTHORIZED, "인증이 만료되었습니다. 다시 로그인해 주세요."),
    INVALID_TOKEN(UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 한번 확인해 주세요."),
    INVALID_AUTH_REQUEST(UNAUTHORIZED, "잘못된 인증 요청입니다."),
    BLACKLISTED_TOKEN(UNAUTHORIZED, "이미 로그아웃된 토큰입니다.");

    private final HttpStatus httpStatus;

    private final String message;

    public static AuthErrorCode fromAuthenticationException(AuthenticationException exception) {
        if (exception instanceof UsernameNotFoundException
            || exception instanceof BadCredentialsException) {
            return INVALID_CREDENTIALS;
        }
        return UNAUTHORIZED_USER;
    }

}
