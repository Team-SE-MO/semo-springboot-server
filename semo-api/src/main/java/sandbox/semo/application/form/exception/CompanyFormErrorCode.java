package sandbox.semo.application.form.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CompanyFormErrorCode implements ErrorCode {

    COMPANY_ALREADY_EXISTS(INTERNAL_SERVER_ERROR, "이미 등록된 회사 입니다."),
    FORM_DOES_NOT_EXIST(NOT_FOUND, "해당 ID의 폼을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;

    private final String message;
}
