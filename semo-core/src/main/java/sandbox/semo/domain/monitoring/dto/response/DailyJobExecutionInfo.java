package sandbox.semo.domain.monitoring.dto.response;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyJobExecutionInfo {

    private Map<String, DailyJobData> executionDate;
}
