package sandbox.semo.application.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import sandbox.semo.application.common.response.ApiResponse;

/**
 * GlobalExceptionHandler: 도메인과 관련된 예외가 아닌 나머지 예외를 핸들링하는 클래스입니다.
 * 기본적인 예외를 처리하기 위해 ResponseEntityExceptionHandler를 상속 받습니다.
 * 400(유효성 검사), 404, 500 에러에 대한 예외를 핸들링합니다.
 * 가장 낮은 우선순위로 예외를 핸들링합니다.
 */
@Order
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 유효성 검사를 수행하여 실패했을 경우 발생하는 ConstraintViolationException 예외를 핸들링하는 메서드입니다.
     * MethodArgumentNotValidException과는 다르게 Bean Validation API를 직접 다룰 때 발생합니다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse> handleConstraintViolationException (
            ConstraintViolationException exception
    ) {
        log.error(">>> [ ❌ ConstraintViolationException: {} ]", exception.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ApiResponse.errorResponse(
                                BAD_REQUEST.value(),
                                exception.getMessage()
                        )
                );
    }

    /**
     * 서버 내부에서 문제 발생시 예외를 핸들링하는 메서드입니다.
     * 어떤 문제인지 파악하기 쉽도록 request의 일부와 exception 스택 트레이스 로그를 출력합니다.
     */
    @ExceptionHandler({Exception.class})
    protected ResponseEntity<ApiResponse> handleException (
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                ">>> [ ❌ 서버 내부에서 문제가 발생했습니다. method: {}, requestURI: {}, exception: {} ]",
                request.getMethod(), request.getRequestURI(), exception
        );
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.errorResponse(
                                INTERNAL_SERVER_ERROR.value(),
                                exception.getMessage()
                        )
                );
    }

}
