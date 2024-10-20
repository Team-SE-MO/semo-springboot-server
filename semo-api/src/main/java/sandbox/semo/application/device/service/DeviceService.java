package sandbox.semo.application.device.service;

import sandbox.semo.domain.device.dto.request.DeviceRegister;

public interface DeviceService {

    void healthCheck(DeviceRegister request);

}
