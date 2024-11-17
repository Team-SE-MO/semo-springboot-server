package sandbox.semo.application.device.service;

import sandbox.semo.domain.common.dto.response.OffsetPage;
import sandbox.semo.domain.device.dto.request.DeviceRegister;
import sandbox.semo.domain.device.dto.request.DataBaseInfo;
import sandbox.semo.domain.device.dto.request.DeviceUpdate;
import sandbox.semo.domain.member.entity.Role;

public interface DeviceService {

    boolean healthCheck(DataBaseInfo request);

    void register(Long companyId, DeviceRegister request);

    OffsetPage<?> findDevices(int page, int size, Role role, Long companyId);

    void update(Long companyId, DeviceUpdate request);

    void deleteDevice(Long companyId, String deviceAlias);
}
