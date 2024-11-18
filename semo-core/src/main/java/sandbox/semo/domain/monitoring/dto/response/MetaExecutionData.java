package sandbox.semo.domain.monitoring.dto.response;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetaExecutionData {

    private Map<String, Double> executionTimes;

}
