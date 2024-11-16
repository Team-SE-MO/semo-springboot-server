package sandbox.semo.application.device.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum DeviceErrorCode implements ErrorCode {

    DEVICE_NOT_FOUND(NOT_FOUND, "존재하지 않는 장치입니다."),
    DATABASE_CONNECTION_FAILURE(INTERNAL_SERVER_ERROR, "데이터베이스 연결에 실패했습니다."),
    ACCESS_DENIED(FORBIDDEN, "테이블에 접근할 수 없습니다.");

    private final HttpStatus httpStatus;

    private final String message;

}
