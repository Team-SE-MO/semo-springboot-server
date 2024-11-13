package sandbox.semo.domain.monitoring.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyJobData {

    @JsonIgnore
    private LocalDate executionDate;
    private Double storeJobDuration;
    private Double retentionJobDuration;
}