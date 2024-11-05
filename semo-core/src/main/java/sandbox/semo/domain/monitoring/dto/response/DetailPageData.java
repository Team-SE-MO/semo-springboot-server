package sandbox.semo.domain.monitoring.dto.response;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetailPageData {

    private String deviceAlias;
    private Map<String, Integer> totalSessions;
    private Map<String, Integer> activeSessions;
    private Map<String, Integer> blockingSessions;
    private Map<String, Integer> waitSessions;
    private Map<String, List<TypeData>> sessionCountGroupByUser;
    private Map<String, List<TypeData>> sessionCountGroupByCommand;
    private Map<String, List<TypeData>> sessionCountGroupByMachine;
    private Map<String, List<TypeData>> sessionCountGroupByType;

}
