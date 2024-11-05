package sandbox.semo.domain.common.util;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.entity.SessionData;

import java.util.function.Consumer;

public class ParameterSourceConverter {

    private static MapSqlParameterSource createParameterSource(Consumer<MapSqlParameterSource> parameterConfigurer) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        parameterConfigurer.accept(params);
        return params;
    }

    public static MapSqlParameterSource convertToSqlParameterSource(boolean status, Long deviceId) {
        return createParameterSource(params -> {
            params.addValue("status", status);
            params.addValue("deviceId", deviceId);
        });
    }

    public static MapSqlParameterSource convertToSqlParameterSource(Long deviceId, LocalDateTime collectedAt) {
        return createParameterSource(params -> {
            params.addValue("deviceId", deviceId);
            params.addValue("collectedAt", collectedAt);
        });
    }

    public static MapSqlParameterSource convertToSqlParameterSource(MonitoringMetric monitoringMetric) {
        return createParameterSource(params -> {
            params.addValue("collectedAt", monitoringMetric.getId().getCollectedAt());
            params.addValue("deviceId", monitoringMetric.getId().getDeviceId());
            params.addValue("totalSessionCount", monitoringMetric.getTotalSessionCount() != null ? monitoringMetric.getTotalSessionCount() : 0);
            params.addValue("activeSessionCount", monitoringMetric.getActiveSessionCount() != null ? monitoringMetric.getActiveSessionCount() : 0);
            params.addValue("sessionCountGroupByUser", monitoringMetric.getSessionCountGroupByUser() != null ? monitoringMetric.getSessionCountGroupByUser() : "-");
            params.addValue("sessionCountGroupByCommand", monitoringMetric.getSessionCountGroupByCommand() != null ? monitoringMetric.getSessionCountGroupByCommand() : "-");
            params.addValue("sessionCountGroupByMachine", monitoringMetric.getSessionCountGroupByMachine() != null ? monitoringMetric.getSessionCountGroupByMachine() : "-");
            params.addValue("sessionCountGroupByType", monitoringMetric.getSessionCountGroupByType() != null ? monitoringMetric.getSessionCountGroupByType() : "-");
            params.addValue("blockingSessionCount", monitoringMetric.getBlockingSessionCount() != null ? monitoringMetric.getBlockingSessionCount() : 0);
            params.addValue("waitSessionCount", monitoringMetric.getWaitSessionCount() != null ? monitoringMetric.getWaitSessionCount() : 0);
        });
    }
}
