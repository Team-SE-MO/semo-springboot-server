package sandbox.semo.application.company.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum CompanyErrorCode implements ErrorCode {

    STATUS_NOT_APPROVED(BAD_REQUEST, "승인되지 않은 상태의 폼 입니다.");

    private final HttpStatus httpStatus;

    private final String message;

}
