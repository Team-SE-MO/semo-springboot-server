package sandbox.semo.application.device.service;

import java.util.List;
import sandbox.semo.domain.device.dto.request.DeviceRegister;
import sandbox.semo.domain.device.dto.request.DataBaseInfo;
import sandbox.semo.domain.device.dto.response.DeviceInfo;
import sandbox.semo.domain.member.entity.Role;

public interface DeviceService {

    boolean healthCheck(DataBaseInfo request);

    void register(Long companyId, DeviceRegister request);

    List<DeviceInfo> getDeviceInfo(Role role, Long companyId);

}
