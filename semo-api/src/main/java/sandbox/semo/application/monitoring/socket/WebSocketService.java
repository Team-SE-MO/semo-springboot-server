package sandbox.semo.application.monitoring.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import sandbox.semo.domain.common.dto.response.CursorPage;
import sandbox.semo.domain.monitoring.dto.request.DeviceMonitoring;
import sandbox.semo.domain.monitoring.dto.response.DetailPageData;
import sandbox.semo.domain.monitoring.dto.response.DeviceRealTimeData;
import sandbox.semo.domain.monitoring.dto.response.SessionDataInfo;

@Log4j2
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
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
            DeviceRealTimeData monitoringData = generateMonitoringData(companyId, deviceAlias);
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

    private DeviceRealTimeData generateMonitoringData(Long companyId, String deviceAlias) {
        LocalDateTime currentTime = LocalDateTime.now();
        int currentSecond = currentTime.getSecond();
        int adjustment = currentSecond % 5;

        LocalDateTime adjustedTime = currentTime.minusSeconds(adjustment).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endTime = adjustedTime.minusSeconds(5).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime startTime = endTime.minusSeconds(4).truncatedTo(ChronoUnit.SECONDS);
        DeviceMonitoring request = DeviceMonitoring.builder()
                .deviceAlias(deviceAlias)
                .interval("5s")
                .startTime(startTime)
                .endTime(endTime)
                .build();
        DetailPageData chartData = monitoringService.fetchDetailData(request, companyId);
        CursorPage<SessionDataInfo> gridData = monitoringService.fetchSessionData(
                deviceAlias, companyId, endTime.toString()
        );
        return DeviceRealTimeData.from(chartData, gridData);
    }

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
    }

}
