package sandbox.semo.batch.service.step;

import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.device.repository.DeviceRepository;

@Log4j2
@RequiredArgsConstructor
public class DeviceStatusReader implements ItemReader<Device>, StepExecutionListener {

    private final DeviceRepository deviceRepository;
    private Iterator<Device> deviceIterator;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.debug(">>> [ 🔍 모든 장비 정보 조회 ]");
        List<Device> deviceList = deviceRepository.findAll();
        deviceIterator = deviceList.iterator();
        log.info(">>> [ 🚀 Device Reader 초기화, 현재 등록 되어 있는 장비 수: {} ]", deviceList.size());
    }

    @Override
    public Device read() {
        if (deviceIterator == null || !deviceIterator.hasNext()) {
            log.info(">>> [ 🎯 더 이상 정보를 읽을 장비가 없습니다. ]");
            return null;
        }

        try {
            Device device = deviceIterator.next();
            log.info(">>> [ 📑 장비 정보 읽기: {} ]", device.getDeviceAlias());
            return device;
        } catch (Exception e) {
            log.error(">>> [ ❌ 장비 정보를 읽는 중 오류 발생: {} ]", e.getMessage());
            return null;
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info(">>> [ ✅ 모든 장비 정보 읽기 완료 ]");
        return ExitStatus.COMPLETED;
    }

}
