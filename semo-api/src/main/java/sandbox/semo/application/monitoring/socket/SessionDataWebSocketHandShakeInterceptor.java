package sandbox.semo.application.monitoring.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class SessionDataWebSocketHandShakeInterceptor implements HandshakeInterceptor {

    private static final int URL_LENGTH = 5;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String[] parts = request.getURI().getPath().split("/");

        if (parts.length != URL_LENGTH || !isNumeric(parts[3])) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getBody().write("웹소켓 연결 실패".getBytes());
            return false;
        }

        Long companyId = Long.parseLong(parts[3]);
        String deviceAlias = parts[4];
        attributes.put("companyId", companyId);
        attributes.put("deviceAlias", deviceAlias);

        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");

        attributes.put("token", token);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        log.info(">>> [ 🤝 Session Data Web Socket Handshake Success! ]");
    }

    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
