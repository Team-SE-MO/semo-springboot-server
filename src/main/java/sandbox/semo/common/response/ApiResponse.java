package sandbox.semo.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * RestAPI 공통 Response 객체
 *
 * @JsonInclude 응답 데이터로 줄 때 null이 발생하는 것들은 응답 데이터로 던지지 못하게 설정
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    @Builder
    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * API 요청 성공시, 응답 데이터로 사용할 successResponse 메서드
     *
     * @param httpStatus - HttpStatus Code
     * @param message - 응답 메세지
     * @param data - 응답 데이터
     */

    public static <T> ApiResponse<T> successResponse(
            HttpStatus httpStatus,
            String message,
            T data
    ) {
        return ApiResponse.<T>builder()
                .code(httpStatus.value())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * API 요청 성공시, 응답 데이터가 없을 경우 사용할 successResponse 메서드
     *
     * @param httpStatus - HttpStatus Code
     * @param message - 응답 메세지
     */
    public static <T> ApiResponse<T> successResponse(
            HttpStatus httpStatus,
            String message
    ) {
        return ApiResponse.<T>builder()
                .code(httpStatus.value())
                .message(message)
                .build();
    }

    /**
     * API 요청 실패시, 사용할 errorResponse 메서드
     *
     * @param code - 상태 코드
     * @param message - 응답 메세지
     */
    public static <T> ApiResponse<T> errorResponse(
            int code,
            String message
    ) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

}
