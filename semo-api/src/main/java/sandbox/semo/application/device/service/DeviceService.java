package sandbox.semo.application.device.service;

import sandbox.semo.domain.device.dto.request.HealthCheck;

public interface DeviceService {

    void healthCheck(HealthCheck request);

}
