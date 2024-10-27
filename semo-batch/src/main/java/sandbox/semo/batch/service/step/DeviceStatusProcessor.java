package sandbox.semo.batch.service.step;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import sandbox.semo.batch.dto.DeviceStatus;
import sandbox.semo.batch.util.HikariDataSourceUtil;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.device.entity.Device;

@Log4j2
@RequiredArgsConstructor
public class DeviceStatusProcessor implements ItemProcessor<Device, DeviceStatus>,
        StepExecutionListener {

    private final AES256 aes256;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> [ 🚀 Device Processor 초기화 ]");
    }

    @Override
    public DeviceStatus process(Device device) {
        HikariDataSource dataSource = null;
        boolean updatedStatus;

        try {
            dataSource = HikariDataSourceUtil.createDataSource(device, aes256);
            dataSource.getConnection().isValid(1);
            updatedStatus = true;
            log.info(">>> [ ✅ Device {} 연결 성공 ]", device.getDeviceAlias());
        } catch (Exception e) {
            updatedStatus = false;
            log.error(">>> [ ❌ Device {} 연결 실패. 상태: 오류. 에러: {} ]",
                    device.getDeviceAlias(),
                    e.getMessage());
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }

        boolean statusChanged = device.getStatus() != updatedStatus;
        return new DeviceStatus(device, statusChanged);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info(">>> [ ✅ 모든 장비 작업 완료 ]");
        return ExitStatus.COMPLETED;
    }

}
