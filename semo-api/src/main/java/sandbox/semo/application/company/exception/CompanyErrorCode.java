package sandbox.semo.application.company.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum CompanyErrorCode implements ErrorCode {

    STATUS_NOT_APPROVED(BAD_REQUEST, "승인되지 않은 상태의 폼 입니다."),
    COMPANY_ALREADY_EXISTS(CONFLICT, "이미 등록된 회사 입니다."),
    FORM_DOES_NOT_EXIST(NOT_FOUND, "해당 ID의 폼을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;

    private final String message;

}
