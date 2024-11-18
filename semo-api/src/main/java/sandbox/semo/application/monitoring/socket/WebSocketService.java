package sandbox.semo.application.monitoring.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sandbox.semo.application.monitoring.service.MonitoringService;
import sandbox.semo.domain.monitoring.dto.request.DeviceMonitoring;
import sandbox.semo.domain.monitoring.dto.response.DetailPageData;

@Log4j2
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MonitoringService monitoringService;

    @PostConstruct
    public void startScheduler() {
        scheduler.scheduleAtFixedRate(this::broadcastMonitoringData, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stopScheduler() {
        scheduler.shutdown();
    }

    public void broadcastMonitoringData() {
        sessions.values().forEach(session -> {
            Long companyId = (Long) session.getAttributes().get("companyId");
            String deviceAlias = (String) session.getAttributes().get("deviceAlias");

            DetailPageData monitoringData = generateMonitoringData(companyId, deviceAlias);

            try {
                if (session.isOpen()) {
                    String message = objectMapper.writeValueAsString(monitoringData);
                    session.sendMessage(new TextMessage(message));
                    log.info("데이터 전송: 세션 ID = {}, companyId = {}, 메시지 = {}", session.getId(), companyId, message);
                }
            } catch (IOException e) {
                log.error("데이터 전송 오류: {}", e.getMessage());
            }
        });
    }

    private DetailPageData generateMonitoringData(Long companyId, String deviceAlias) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusSeconds(5);
        DeviceMonitoring request = DeviceMonitoring.builder()
                .deviceAlias(deviceAlias)
                .interval("5s")
                .startTime(startTime)
                .endTime(endTime)
                .build();
        return monitoringService.fetchDetailData(request, companyId);
    }

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
    }

}
