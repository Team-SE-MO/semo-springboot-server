package sandbox.semo.domain.monitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConnectInfo {

    private String deviceAlias;
    private String type;
    private String status;
    private String sid;
    private String ip;
    private Long port;
    private Long statusValue;

}
