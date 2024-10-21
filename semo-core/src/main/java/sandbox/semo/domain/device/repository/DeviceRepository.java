package sandbox.semo.domain.device.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.device.dto.response.DeviceInfo;
import sandbox.semo.domain.device.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    @Query("SELECT new sandbox.semo.domain.device.dto.response.DeviceInfo" +
            "(d.deviceAlias, d.type, d.ip, d.port, d.sid, d.status, d.updatedAt) " +
            "FROM Device d " +
            "WHERE d.company.id = :companyId")
    List<DeviceInfo> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT new sandbox.semo.domain.device.dto.response.DeviceInfo" +
            "(d.deviceAlias, d.type, d.ip, d.port, d.sid, d.status, d.updatedAt) " +
            "FROM Device d " +
            "WHERE d.company.id <> :companyId")
    List<DeviceInfo> findAllExceptByCompanyId(@Param("companyId") Long companyId);

}
