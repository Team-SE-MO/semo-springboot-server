package sandbox.semo.application.security.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS(BAD_REQUEST,  "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_USER(UNAUTHORIZED, "인증 되지 않은 사용자 입니다. 다시 한번 확인해 주세요."),
    TOKEN_EXPIRED(UNAUTHORIZED, "인증이 만료되었습니다. 다시 로그인해 주세요.");

    private final HttpStatus httpStatus;

    private final String message;

}
