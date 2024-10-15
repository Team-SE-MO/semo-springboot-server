package sandbox.semo.security.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_CREDENTIALS("아이디 또는 비밀번호가 일치하지 않습니다.", BAD_REQUEST),
    UNAUTHORIZED_USER("인증 되지 않은 사용자 입니다. 다시 한번 확인해 주세요.", UNAUTHORIZED);

    private final String message;
    private final HttpStatus status;

}
