package sandbox.semo.domain.monitoring.dto.response;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeSeriesData {

    private int totalSessions;

    private int activeSessions;

    private int blockingSessions;

    private int waitingSessions;

    private Map<String, Integer> sessionCountGroupByUser;

    private Map<String, Integer> sessionCountGroupByCommand;

    private Map<String, Integer> sessionCountGroupByMachine;

    private Map<String, Integer> sessionCountGroupByType;

}
