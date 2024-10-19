package sandbox.semo.application.form.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyFormErrorCode {

    COMPANY_ALREADY_EXISTS(HttpStatus.INTERNAL_SERVER_ERROR, "이미 등록된 회사 입니다."),
    FORM_NO_FOUND(HttpStatus.NOT_FOUND, "조회된 데이터가 없습니다.");

    private final HttpStatus httpStatus;

    private final String message;
}
