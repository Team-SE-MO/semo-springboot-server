package sandbox.semo.batch.service.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import sandbox.semo.batch.dto.DeviceInfo;
import sandbox.semo.batch.repository.JdbcRepository;
import sandbox.semo.domain.collection.entity.MonitoringMetric;
import sandbox.semo.domain.collection.entity.SessionData;
import sandbox.semo.domain.device.entity.Device;

@Log4j2
@RequiredArgsConstructor
public class DeviceWriter implements ItemWriter<DeviceInfo>, StepExecutionListener {

    private final JdbcRepository jdbcRepository;
    private final Object lock = new Object();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> [ 🚀 Device Writer 초기화 ]");
    }

    @Override
    public void write(Chunk<? extends DeviceInfo> chunk) {
        log.info(">>> [ ✍️ Writing chunk in thread: {} ]", Thread.currentThread().getName());
        
        List<DeviceInfo> items = new ArrayList<>(chunk.getItems());
        
        // 디바이스 상태 업데이트를 동기화하여 처리
        items.forEach(item -> {
            if (item.isStatusChanged()) {
                synchronized (lock) {
                    updateDeviceStatus(item.getDevice());
                }
            } else {
                logSkippedUpdate(item.getDevice());
            }
        });

        // 세션 데이터 일괄 처리
        List<SessionData> allSessionData = items.stream()
                .flatMap(item -> item.getSessionDataList().stream())
                .collect(Collectors.toList());
        if (!allSessionData.isEmpty()) {
            saveSessionData(allSessionData);
        }

        // 모니터링 메트릭 일괄 처리
        List<MonitoringMetric> metrics = items.stream()
                .map(DeviceInfo::getMonitoringMetric)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!metrics.isEmpty()) {
            saveMonitoringMetrics(metrics);
        }
    }

    private void updateDeviceStatus(Device device) {
        try {
            boolean updateStatus = !device.getStatus();
            jdbcRepository.deviceStatusUpdate(updateStatus, device.getId());
            log.info(">>> [ 🔄 Device {} 상태 변경. 업데이트 상태: {} - Thread: {} ]",
                    device.getDeviceAlias(),
                    updateStatus,
                    Thread.currentThread().getName()
            );
        } catch (Exception e) {
            log.error(">>> [ ❌ Device {} 상태 변경 중 오류 발생: {} ]",
                    device.getDeviceAlias(),
                    e.getMessage());
        }
    }

    private void logSkippedUpdate(Device device) {
        log.info(">>> [ ⏭️ Device {} 상태 변경 없음. 업데이트 생략 ]", device.getDeviceAlias());
    }

    private void saveSessionData(List<SessionData> sessionDataList) {
        try {
            jdbcRepository.saveSessionData(sessionDataList);
            log.info(">>> [ 💾 SessionData 저장 완료. 총 데이터 개수: {} - Thread: {} ]", 
                    sessionDataList.size(),
                    Thread.currentThread().getName());
        } catch (Exception e) {
            log.error(">>> [ ❌ SessionData 저장 중 오류 발생: {} ]", e.getMessage());
        }
    }

    private void saveMonitoringMetrics(List<MonitoringMetric> metrics) {
        try {
            metrics.forEach(jdbcRepository::saveMonitoringMetric);
            log.info(">>> [ 💾 MonitoringMetric 저장 완료 - Thread: {} ]",
                    Thread.currentThread().getName());
        } catch (Exception e) {
            log.error(">>> [ ❌ MonitoringMetric 저장 중 오류 발생: {} ]", e.getMessage());
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info(">>> [ ✅ 모든 장비 쓰기 완료 ]");
        return ExitStatus.COMPLETED;
    }

}
