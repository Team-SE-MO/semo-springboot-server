package sandbox.semo.application.file.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sandbox.semo.application.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {

    FILE_DOWNLOAD_FAILURE(INTERNAL_SERVER_ERROR, "파일 다운로드에 실패했습니다."),
    FILE_NOT_FOUND(NOT_FOUND, "파일을 찾을 수 없습니다."),
    DIRECTORY_CREATE_FAILED(INTERNAL_SERVER_ERROR,"폴더 생성에 실패했습니다."),
    FILE_CREATE_ERROR(INTERNAL_SERVER_ERROR,"회사 파일 생성에 실패했습니다."),
    DEVICE_ID_COLUMN_NOT_FOUND(INTERNAL_SERVER_ERROR, "DEVICE_ID 컬럼을 찾을 수 없습니다."),
    NO_MATCHING_DEVICE_DATA(INTERNAL_SERVER_ERROR, "해당 DEVICE_ID의 데이터가 없습니다."),
    CSV_PROCESSING_ERROR(INTERNAL_SERVER_ERROR, "CSV 파일 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;

    private final String message;
}
