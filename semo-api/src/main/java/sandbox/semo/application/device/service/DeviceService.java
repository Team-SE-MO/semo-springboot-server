package sandbox.semo.application.device.service;

import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.device.dto.request.DeviceRegister;
import sandbox.semo.domain.device.dto.request.DataBaseInfo;

public interface DeviceService {

    boolean healthCheck(DataBaseInfo request);

    void register(Company company, DeviceRegister request);

}
