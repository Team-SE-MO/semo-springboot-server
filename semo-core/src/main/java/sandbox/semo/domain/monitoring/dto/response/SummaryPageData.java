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
public class SummaryPageData {

    private String companyName;

    private TotalProcessInfo totalProcessInfo;

    private Map<String, DeviceConnectInfo> allDevices;

}

