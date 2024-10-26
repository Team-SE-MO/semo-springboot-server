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
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(NOT_FOUND, "존재하지 않는 사용자입니다."),
    UNAUTHORIZED_TO_MEMBER(FORBIDDEN, "권한이 없는 사용자입니다."),
    WRONG_PASSWORD(BAD_REQUEST, "입력하신 비밀번호를 다시 확인해 주세요."),
    COMPANY_NOT_EXIST(NOT_FOUND, "존재하지 않는 회사입니다."),
    FORM_DOES_NOT_EXIST(NOT_FOUND, "해당 ID의 폼을 찾을 수 없습니다."),
    INVALID_COMPANY_SELECTION(BAD_REQUEST, "선택할 수 없는 회사입니다.");

    private final HttpStatus httpStatus;

    private final String message;

}
