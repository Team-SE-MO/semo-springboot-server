package sandbox.semo.batch.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import sandbox.semo.domain.device.entity.Device;

@Data
@RequiredArgsConstructor
public class DeviceStatus {

    private final Device device;
    private final boolean statusChanged;

}
