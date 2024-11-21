package sandbox.semo.domain.monitoring.dto.response;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import sandbox.semo.domain.common.dto.response.CursorPage;

@Data
@Builder
public class DeviceRealTimeData {

    private String deviceAlias;
    private Map<String, Integer> totalSessions;
    private Map<String, Integer> activeSessions;
    private Map<String, Integer> blockingSessions;
    private Map<String, Integer> waitSessions;
    private Map<String, List<TypeData>> sessionCountGroupByUser;
    private Map<String, List<TypeData>> sessionCountGroupByCommand;
    private Map<String, List<TypeData>> sessionCountGroupByMachine;
    private Map<String, List<TypeData>> sessionCountGroupByType;

    private List<SessionDataInfo> sessionDataInfos;

    public static DeviceRealTimeData from(
            DetailPageData chartData, CursorPage<SessionDataInfo> gridData
    ) {
        return DeviceRealTimeData.builder()
                .deviceAlias(chartData.getDeviceAlias())
                .totalSessions(chartData.getTotalSessions())
                .activeSessions(chartData.getActiveSessions())
                .blockingSessions(chartData.getBlockingSessions())
                .waitSessions(chartData.getWaitSessions())
                .sessionCountGroupByUser(chartData.getSessionCountGroupByUser())
                .sessionCountGroupByCommand(chartData.getSessionCountGroupByCommand())
                .sessionCountGroupByMachine(chartData.getSessionCountGroupByMachine())
                .sessionCountGroupByType(chartData.getSessionCountGroupByType())
                .sessionDataInfos(gridData.getContent())
                .build();
    }

}
