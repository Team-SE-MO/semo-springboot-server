package sandbox.semo.batch.dto;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import sandbox.semo.domain.collection.entity.SessionData;
import sandbox.semo.domain.device.entity.Device;

@Data
@RequiredArgsConstructor
public class DeviceStatus {

    private final Device device;
    private final boolean statusChanged;
    private final List<SessionData> sessionDataList;

}
