package sandbox.semo.application.monitoring.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
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
import sandbox.semo.domain.monitoring.dto.response.MetaExecutionData;
import sandbox.semo.domain.monitoring.repository.MonitoringRepository;

@Log4j2
@Service
@RequiredArgsConstructor
public class BatchDataMonitoringWebSocketService {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    private final MonitoringRepository monitoringRepository;

    @PostConstruct
    public void startScheduler() {
        log.info(">>> [ 🔄 Batch Data Monitoring WebSocket 데이터 전송 스케줄러 시작 ]");
        scheduler.scheduleAtFixedRate(this::broadcastData, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stopScheduler() {
        log.info(">>> [ 💤 Batch Data Monitoring WebSocket 데이터 전송 스케줄러 종료 ]");
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

    public MetaExecutionData getInitialMonitoringData() {
        MetaExecutionData data = monitoringRepository.findRealTimeJobExecutionTimes();
        log.info(">>> [ ⏳ 초기 데이터 조회 ] => data: {}", data);
        return data;
    }

    private void broadcastData() {
        sessions.values().forEach(session -> {
            log.info(">>> [ ⏱️ 실시간 데이터 조회 ]");
            MetaExecutionData data = monitoringRepository.findRealTimeJobExecutionTimesByLastTime();
            try {
                if (session.isOpen()) {
                    String message = objectMapper.writeValueAsString(data);
                    session.sendMessage(new TextMessage(message));
                    log.info(">>> [ 🚀 데이터 전송 ] => message = {}", message);
                }
            } catch (IOException e) {
                log.error(">>> [ 🚨 데이터 전송 오류 ] => {}", e.getMessage());
            }
        });
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
