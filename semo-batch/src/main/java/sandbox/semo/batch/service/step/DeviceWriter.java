package sandbox.semo.batch.service.step;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import sandbox.semo.batch.dto.DeviceInfo;
import sandbox.semo.batch.repository.JdbcRepository;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.device.entity.Device;

@Log4j2
@RequiredArgsConstructor
public class DeviceWriter implements ItemWriter<DeviceInfo>, StepExecutionListener {

    private final JdbcRepository jdbcRepository;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> [ 🚀 Device Writer 초기화 ]");
    }

    @Override
    public void write(Chunk<? extends DeviceInfo> chunk) {
        chunk.getItems().forEach(this::processDeviceCollection);
    }

    private void processDeviceCollection(DeviceInfo item) {
        Device device = item.getDevice();
        if (item.isStatusChanged()) {
            updateDeviceStatus(device);
        } else {
            logSkippedUpdate(device);
        }

        if (!item.getSessionDataList().isEmpty()) {
            saveSessionData(item.getSessionDataList());
        }

        if (item.getMonitoringMetric() != null) {
            saveMonitoringMetric(item.getMonitoringMetric());
        }
    }

    private void updateDeviceStatus(Device device) {
        try {
            boolean updateStatus = !device.getStatus();
            jdbcRepository.deviceStatusUpdate(updateStatus, device.getId());
            log.info(">>> [ 🔄 Device {} 상태 변경. 업데이트 상태: {} ]",
                    device.getDeviceAlias(),
                    updateStatus
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
            log.info(">>> [ 💾 SessionData 저장 완료. 총 데이터 개수: {} ]", sessionDataList.size());
        } catch (Exception e) {
            log.error(">>> [ ❌ SessionData 저장 중 오류 발생: {} ]", e.getMessage());
        }
    }

    private void saveMonitoringMetric(MonitoringMetric monitoringMetric) {
        try {
            jdbcRepository.saveMonitoringMetric(monitoringMetric);
            log.info(">>> [ 💾 MonitoringMetric 저장 완료 ]");
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
