package sandbox.semo.application.email.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum EmailErrorCode implements ErrorCode {

    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    EMAIL_TEMPLATE_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 템플릿 로드에 실패했습니다."),
    MEMBER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "사용자를 찾을 수 없습니다."),
    COMPANY_NAME_MISSING(HttpStatus.BAD_REQUEST, "회사 이름이 누락되었습니다."),
    APPROVAL_DENIED(HttpStatus.BAD_REQUEST, "아직 승인되지 않았습니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청 입니다." ),
    INVALID_EMAIL_ADDRESS(HttpStatus.BAD_REQUEST, "유효하지 않은 주소 입니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
