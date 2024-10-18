package sandbox.semo.application.security.util;

import static org.springframework.http.HttpStatus.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import sandbox.semo.application.security.exception.AuthErrorCode;

public class JsonResponseHelper {

    public static void sendJsonSuccessResponse(
            HttpServletResponse response, Map<String, Object> responseBody
    ) throws IOException {
        response.setStatus(OK.value());
        response.setContentType("application/json;charset=UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(responseBody));
    }

    public static void sendJsonErrorResponse(HttpServletResponse response, AuthErrorCode authErrorCode)
            throws IOException {
        response.setStatus(authErrorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", authErrorCode.getHttpStatus().value());
        responseBody.put("message", authErrorCode.getMessage());

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(responseBody));
    }

}
