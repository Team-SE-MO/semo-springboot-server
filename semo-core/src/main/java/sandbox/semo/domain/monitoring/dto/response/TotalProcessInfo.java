package sandbox.semo.domain.monitoring.dto.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalProcessInfo {

    private int activeDeviceCnt;

    private int inActiveDeviceCnt;

    private int blockedDeviceCnt;

    private Map<String, Integer> topUsedDevices;

    private Map<String, Integer> warnDevice;

    private Map<String, Integer> unUsedDevice;

}
