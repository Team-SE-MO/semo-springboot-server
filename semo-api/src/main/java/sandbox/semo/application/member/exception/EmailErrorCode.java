package sandbox.semo.application.member.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum EmailErrorCode implements ErrorCode {

    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    COMPANY_NAME_MISSING(HttpStatus.BAD_REQUEST, "회사 이름이 누락되었습니다."),
    OWNER_NAME_MISSING(HttpStatus.BAD_REQUEST, "소유자 이름이 누락되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
