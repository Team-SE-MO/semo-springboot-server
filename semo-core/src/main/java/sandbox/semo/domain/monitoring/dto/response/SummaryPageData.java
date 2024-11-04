package sandbox.semo.domain.monitoring.dto.response;

import java.util.List;
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

    private List<DeviceConnectInfo> allDevices;

}

