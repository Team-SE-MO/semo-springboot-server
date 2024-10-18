package sandbox.semo.form.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyFormErrorCode {

    COMPANY_ALREADY_EXISTS(INTERNAL_SERVER_ERROR, "이미 등록된 회사 입니다.");

    private final HttpStatus httpStatus;

    private final String message;
}
