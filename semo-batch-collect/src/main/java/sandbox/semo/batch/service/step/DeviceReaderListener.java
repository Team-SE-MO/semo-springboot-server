package sandbox.semo.batch.service.step;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import sandbox.semo.domain.device.entity.Device;

@Log4j2
@Component
public class DeviceReaderListener implements ItemReadListener<Device>, StepExecutionListener {

    private AtomicInteger totalCount = new AtomicInteger(0);

    // StepExecutionListener 메서드
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> [ 🔍 장비 정보 조회 시작 ]");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info(">>> [ ✅ 모든 장비 정보 읽기 완료, 총 처리된 장비 수: {} ]",
            totalCount.get());
        totalCount.set(0);
        return ExitStatus.COMPLETED;
    }

    // ItemReadListener 메서드
    @Override
    public void afterRead(Device device) {
        log.info(">>> [ 📑 장비 정보 읽기: {} - Thread: {} - 처리된 장비 수: {} ]",
            device.getDeviceAlias(),
            Thread.currentThread().getName(),
            totalCount.incrementAndGet());
    }

    @Override
    public void onReadError(Exception ex) {
        log.error(">>> [ ❌ 장비 정보 읽기 실패: {} ]", ex.getMessage());
    }
}
