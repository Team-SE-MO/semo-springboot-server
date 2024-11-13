package sandbox.semo.application.email.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum EmailErrorCode implements ErrorCode {

    EMAIL_SEND_FAILED(INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    EMAIL_TEMPLATE_LOAD_FAILED(INTERNAL_SERVER_ERROR, "이메일 템플릿을 불러오는데 실패했습니다."),
    COMPANY_NAME_MISSING(BAD_REQUEST, "회사 이름이 누락 되었습니다."),
    APPROVAL_DENIED(BAD_REQUEST, "승인 처리 되지 않은 요청 입니다."),
    INVALID_AUTH_CODE(BAD_REQUEST, "인증 코드가 불일치 합니다."),
    INVALID_REQUEST(BAD_REQUEST, "유효하지 않은 요청 입니다." ),
    INVALID_EMAIL_ADDRESS(BAD_REQUEST, "유효하지 않은 주소 입니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
