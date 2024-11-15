package sandbox.semo.application.file.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {

    FILE_DOWNLOAD_FAILURE(INTERNAL_SERVER_ERROR, "파일 다운로드에 실패했습니다."),
    FILE_NOT_FOUND(INTERNAL_SERVER_ERROR, "파일을 찾을 수 없습니다."),
    DEVICE_ID_COLUMN_NOT_FOUND(INTERNAL_SERVER_ERROR, "DEVICE_ID 컬럼을 찾을 수 없습니다."),
    NO_MATCHING_DEVICE_DATA(INTERNAL_SERVER_ERROR, "해당 DEVICE_ID의 데이터가 없습니다.");

    private final HttpStatus httpStatus;

    private final String message;
}
