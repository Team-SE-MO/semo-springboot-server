package sandbox.semo.domain.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.domain.device.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {

}
