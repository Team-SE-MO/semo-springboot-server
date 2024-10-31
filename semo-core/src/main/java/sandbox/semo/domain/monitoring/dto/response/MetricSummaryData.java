package sandbox.semo.domain.monitoring.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricSummaryData {

    private int activeDeviceCount;
    private int inactiveDeviceCount;
    private int blockedDeviceCount;
    private String topUsedDevices;
    private String warnDevices;
    private String unusedDevices;
    private String allDevices;

}
