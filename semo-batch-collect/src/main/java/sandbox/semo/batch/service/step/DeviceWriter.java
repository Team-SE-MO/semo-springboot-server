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
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.monitoring.dto.request.DeviceCollectionInfo;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.monitoring.repository.MonitoringRepository;

@Log4j2
@RequiredArgsConstructor
public class DeviceWriter implements ItemWriter<DeviceCollectionInfo>, StepExecutionListener {

    private final MonitoringRepository monitoringRepository;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> [ 🚀 Device Writer 초기화 ]");
    }

    @Override
    public void write(Chunk<? extends DeviceCollectionInfo> chunk) {
        log.info(">>> [ ✍️ Writing chunk in thread: {} ]", Thread.currentThread().getName());

        List<DeviceCollectionInfo> items = new ArrayList<>(chunk.getItems());

        writeDeviceStatus(items);
        writeSessionData(items);
        writeMonitoringMetrics(items);

    }

    private void writeDeviceStatus(List<? extends DeviceCollectionInfo> items) {
        items.parallelStream()
            .filter(DeviceCollectionInfo::isStatusChanged)
            .forEach(item -> updateDeviceStatus(item.getDevice()));
    }

    private void writeSessionData(List<? extends DeviceCollectionInfo> items) {
        List<SessionData> allSessionData = items.parallelStream()
            .map(DeviceCollectionInfo::getSessionDataList)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        saveSessionData(allSessionData);
    }

    private void writeMonitoringMetrics(List<? extends DeviceCollectionInfo> items) {
        List<MonitoringMetric> metrics = items.parallelStream()
            .map(DeviceCollectionInfo::getMonitoringMetric)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        saveMonitoringMetrics(metrics);
    }

    private void updateDeviceStatus(Device device) {
        try {
            boolean updateStatus = !device.getStatus();
            monitoringRepository.deviceStatusUpdate(updateStatus, device.getId());
            log.info(">>> [ 🔄 Device {} 상태 변경. 업데이트 상태: {} - Thread: {} ]",
                device.getDeviceAlias(),
                updateStatus,
                Thread.currentThread().getName());
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
            monitoringRepository.saveSessionData(sessionDataList);
            log.info(">>> [ 💾 SessionData 저장 완료. 총 데이터 개수: {} - Thread: {} ]",
                sessionDataList.size(),
                Thread.currentThread().getName());
        } catch (Exception e) {
            log.error(">>> [ ❌ SessionData 저장 중 오류 발생: {} ]", e.getMessage());
        }
    }

    private void saveMonitoringMetrics(List<MonitoringMetric> metrics) {
        try {
            metrics.forEach(monitoringRepository::saveMonitoringMetric);
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
