package sandbox.semo.domain.monitoring.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricSummary {

    private String deviceAlias;

    private String type;

    private String ip;

    private Long port;

    private String sid;

    private String status;

    private Long statusValue;

    private LocalDateTime lastCollectedAt;

}
