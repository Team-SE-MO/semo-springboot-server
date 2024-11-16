package sandbox.semo.domain.monitoring.dto.response;


import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StepData {

    private int totalCount;
    private Map<String, Integer> errorTypes;
    private boolean hasError;

    @Builder
    public StepData(int totalCount, Map<String, Integer> errorTypes, boolean hasError) {
        this.totalCount = totalCount;
        this.errorTypes = errorTypes;
        this.hasError = hasError;
    }
}
