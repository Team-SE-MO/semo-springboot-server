package sandbox.semo.domain.monitoring.dto.request;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.device.entity.Device;

@Data
@Builder
@RequiredArgsConstructor
public class DeviceCollectionInfo {

    private final Device device;
    private final boolean statusChanged;
    private final List<SessionData> sessionDataList;
    private final MonitoringMetric monitoringMetric;

}
