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
public class SessionDataMonitoringWebSocketService {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    private final MonitoringService monitoringService;
    private volatile LocalDateTime checkTime; // 중복 시간 체크를 위한 필드 변수

    @PostConstruct
    public void startScheduler() {
        log.info(">>> [ 🔄 WebSocket 데이터 전송 스케줄러 시작 ]");
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime truncatedNow = truncateToNearestInterval(now);
            broadcastMonitoringData(truncatedNow);
        }, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stopScheduler() {
        log.info(">>> [ 💤 WebSocket 데이터 전송 스케줄러 종료 ]");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn(">>> [ ❌ 스케줄러 강제 종료 ]");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error(">>> [ ❌ 스케줄러 종료 중단: {} ]", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public DeviceRealTimeData getInitialMonitoringData(Long companyId, String deviceAlias) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = truncateToNearestInterval(currentTime).minusSeconds(5);
        LocalDateTime startTime = endTime.minusMinutes(1);
        checkTime = endTime;
        log.info(">>> [ ⏳ 초기 데이터 조회 ] => companyId = {}, deviceAlias = {}, currentTime = {}, startTime = {}, endTime = {}",
                companyId, deviceAlias, currentTime, startTime, endTime);

        return fetchMonitoringData(companyId, deviceAlias, startTime, endTime);
    }

    public void broadcastMonitoringData(LocalDateTime truncatedNow) {
        if (truncatedNow.equals(checkTime)) {
            log.info(">>> [ ⏹️ 중복 데이터 제외 ] currentTime = {}, checkTime = {}", truncatedNow, checkTime);
            return; // 중복된 시간은 처리하지 않음
        }

        sessions.values().forEach(session -> {
            Long companyId = (Long) session.getAttributes().get("companyId");
            String deviceAlias = (String) session.getAttributes().get("deviceAlias");

            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime endTime = truncateToNearestInterval(currentTime).minusSeconds(5);
            LocalDateTime startTime = endTime.minusSeconds(2);

            log.info(">>> [ ⏱️ 실시간 데이터 조회 ] => companyId = {}, deviceAlias = {}, collectedAt = {}",
                    companyId, deviceAlias, endTime);

            DeviceRealTimeData monitoringData = fetchMonitoringData(
                    companyId, deviceAlias, startTime, endTime
            );
            try {
                if (session.isOpen()) {
                    String message = objectMapper.writeValueAsString(monitoringData);
                    session.sendMessage(new TextMessage(message));
                    log.info(">>> [ 🚀 데이터 전송 ] => companyId = {}, message = {}", companyId, message);
                }
            } catch (IOException e) {
                log.error(">>> [ 🚨 데이터 전송 오류 ] => {}", e.getMessage());
            }
        });

        checkTime = truncatedNow; // 데이터 전송 성공 후 시간 업데이트
    }

    private DeviceRealTimeData fetchMonitoringData(
            Long companyId,
            String deviceAlias,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        log.debug(">>> [ 🔍 데이터 조회 ] => companyId = {}, deviceAlias = {}, startTime = {}, endTime = {}", companyId, deviceAlias, startTime, endTime);

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

    private LocalDateTime truncateToNearestInterval(LocalDateTime dateTime) {
        int seconds = dateTime.getSecond();
        int adjustment = seconds % 5;
        return dateTime.minusSeconds(adjustment).truncatedTo(ChronoUnit.SECONDS);
    }

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info(">>> [ 🌟 세션 추가 ] => 세션 ID = {}, 총 세션 수 = {}", session.getId(), sessions.size());
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
        log.info(">>> [ ␡ 세션 제거 ] => 세션 ID = {}, 총 세션 수 = {}", session.getId(), sessions.size());
    }

}
