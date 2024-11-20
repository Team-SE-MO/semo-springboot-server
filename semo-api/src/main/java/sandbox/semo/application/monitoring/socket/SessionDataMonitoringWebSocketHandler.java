package sandbox.semo.application.monitoring.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sandbox.semo.domain.monitoring.dto.response.DeviceRealTimeData;

@Log4j2
@Component
@RequiredArgsConstructor
public class SessionDataMonitoringWebSocketHandler extends TextWebSocketHandler {

    private final SessionDataMonitoringWebSocketService sessionDataMonitoringWebSocketService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long companyId = (Long) session.getAttributes().get("companyId");
        String deviceAlias = (String) session.getAttributes().get("deviceAlias");

        DeviceRealTimeData initialData = sessionDataMonitoringWebSocketService.getInitialMonitoringData(companyId, deviceAlias);
        if (session.isOpen()) {
            String message = objectMapper.writeValueAsString(initialData);
            session.sendMessage(new TextMessage(message));
            log.info(">>> [ 🚀 초기 데이터 전송: {}", message);
        }

        sessionDataMonitoringWebSocketService.addSession(session);
        log.info(">>> [ ✅ SessionData Monitoring WebSocket 연결 성공 ] => 세션 ID = {}, companyId = {}, deviceAlias = {}", session.getId(), companyId, deviceAlias);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionDataMonitoringWebSocketService.removeSession(session);
        log.info(">>> [ 👋 SessionData Monitoring WebSocket 연결 종료 ] => 세션 ID = {}", session.getId());
    }

}
