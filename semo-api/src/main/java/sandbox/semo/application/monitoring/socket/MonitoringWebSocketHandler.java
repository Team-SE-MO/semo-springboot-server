package sandbox.semo.application.monitoring.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Log4j2
@Component
@RequiredArgsConstructor
public class MonitoringWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketService webSocketService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long companyId = (Long) session.getAttributes().get("companyId");
        String deviceAlias = (String) session.getAttributes().get("deviceAlias");
        webSocketService.addSession(session);
        log.info("WebSocket 연결 성공: 세션 ID = {}, companyId = {}, deviceAlias = {}", session.getId(), companyId, deviceAlias);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketService.removeSession(session);
        log.info("WebSocket 연결 종료: 세션 ID = {}", session.getId());
    }

}
