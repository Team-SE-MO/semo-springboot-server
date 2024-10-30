package sandbox.semo.batch.service.step;

import com.zaxxer.hikari.HikariDataSource;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import sandbox.semo.batch.dto.DeviceInfo;
import sandbox.semo.batch.repository.JdbcRepository;
import sandbox.semo.batch.util.HikariDataSourceUtil;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.device.entity.Device;

@Log4j2
@RequiredArgsConstructor
public class DeviceProcessor implements ItemProcessor<Device, DeviceInfo>,
        StepExecutionListener {

    private final AES256 aes256;
    private final JdbcRepository jdbcRepository;
    private LocalDateTime collectedAt;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        collectedAt = LocalDateTime.now().withNano(0);
        log.info(">>> [ 🚀 Device Processor 초기화 ]");
    }

    @Override
    public DeviceInfo process(Device device) {
        HikariDataSource dataSource = null;
        boolean updatedStatus;
        List<SessionData> sessionDataList = null;
        MonitoringMetric monitoringMetric = null;

        try {
            dataSource = HikariDataSourceUtil.createDataSource(device, aes256);
            updatedStatus = checkDeviceConnection(dataSource, device);
            sessionDataList = jdbcRepository.fetchSessionData(dataSource, device, collectedAt);
            monitoringMetric = jdbcRepository.fetchMetricData(dataSource, device, collectedAt);
        } catch (Exception e) {
            updatedStatus = false;
            log.error(">>> [ ❌ Device {} 연결 실패. 상태: 오류. 에러: {} ]",
                    device.getDeviceAlias(),
                    e.getMessage());
        } finally {
            closeDataSource(dataSource);
        }

        boolean statusChanged = device.getStatus() != updatedStatus;
        return DeviceInfo.builder()
                .device(device)
                .statusChanged(statusChanged)
                .sessionDataList(sessionDataList)
                .monitoringMetric(monitoringMetric)
                .build();
    }

    private boolean checkDeviceConnection(HikariDataSource dataSource, Device device) throws Exception {
        if (dataSource.getConnection().isValid(1)) {
            log.info(">>> [ ✅ Device {} 연결 성공 ]", device.getDeviceAlias());
            return true;
        }
        return false;
    }

    private void closeDataSource(HikariDataSource dataSource) {
        if (dataSource != null) {
            dataSource.close();
            log.info(">>> [ 🔌 DataSource Close - 리소스 제거 ]");
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info(">>> [ ✅ 모든 장비 작업 완료 ]");
        return ExitStatus.COMPLETED;
    }

}
