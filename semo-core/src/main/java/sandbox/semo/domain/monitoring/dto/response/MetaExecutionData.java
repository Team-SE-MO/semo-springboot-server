package sandbox.semo.domain.monitoring.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
public class MetaExecutionData {
    private Map<String, Double> executionTimes;
} 