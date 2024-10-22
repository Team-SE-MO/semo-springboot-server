package sandbox.semo.application.form.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberFormErrorCode {

    COMPANY_NOT_EXIST(NOT_FOUND, "존재하지 않는 회사입니다.");

    private final HttpStatus httpStatus;

    private final String message;
}
