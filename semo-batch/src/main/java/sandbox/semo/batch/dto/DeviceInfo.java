package sandbox.semo.batch.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import sandbox.semo.domain.collection.entity.MonitoringMetric;
import sandbox.semo.domain.collection.entity.SessionData;
import sandbox.semo.domain.device.entity.Device;

@Data
@Builder
@RequiredArgsConstructor
public class DeviceInfo {

    private final Device device;
    private final boolean statusChanged;
    private final List<SessionData> sessionDataList;
    private final MonitoringMetric monitoringMetric;

}
