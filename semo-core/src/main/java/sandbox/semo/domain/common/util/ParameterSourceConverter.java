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

    public static SqlParameterSource[] convertToSqlParameterSourceArray(
            List<SessionData> sessionDataList) {
        return sessionDataList.stream()
                .map(ParameterSourceConverter::convertToSqlParameterSource)
                .toArray(SqlParameterSource[]::new);
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

    public static MapSqlParameterSource convertToSqlParameterSource(SessionData sessionData) {
        return createParameterSource(params -> {
            params.addValue("collectedAt", sessionData.getId().getCollectedAt());
            params.addValue("sid", sessionData.getId().getSid());
            params.addValue("deviceId", sessionData.getId().getDeviceId());
            params.addValue("serial", sessionData.getSerial() != null ? sessionData.getSerial() : 0);
            params.addValue("username", sessionData.getUsername() != null ? sessionData.getUsername() : "-");
            params.addValue("command", sessionData.getCommand() != null ? sessionData.getCommand() : 0);
            params.addValue("commandName", sessionData.getCommandName() != null ? sessionData.getCommandName() : "-");
            params.addValue("status", sessionData.getStatus() != null ? sessionData.getStatus() : "-");
            params.addValue("schemaName", sessionData.getSchemaName() != null ? sessionData.getSchemaName() : "-");
            params.addValue("osUser", sessionData.getOsUser() != null ? sessionData.getOsUser() : "-");
            params.addValue("process", sessionData.getProcess() != null ? sessionData.getProcess() : "-");
            params.addValue("machine", sessionData.getMachine() != null ? sessionData.getMachine() : "-");
            params.addValue("port", sessionData.getPort() != null ? sessionData.getPort() : -1);
            params.addValue("terminal", sessionData.getTerminal() != null ? sessionData.getTerminal() : "-");
            params.addValue("program", sessionData.getProgram() != null ? sessionData.getProgram() : "-");
            params.addValue("type", sessionData.getType() != null ? sessionData.getType() : "-");
            params.addValue("sqlId", sessionData.getSqlId() != null ? sessionData.getSqlId() : "-");
            params.addValue("sqlExecStart", sessionData.getSqlExecStart());
            params.addValue("sqlExecId", sessionData.getSqlExecId() != null ? sessionData.getSqlExecId() : 0);
            params.addValue("sqlText", sessionData.getSqlText() != null ? sessionData.getSqlText() : "-");
            params.addValue("module", sessionData.getModule() != null ? sessionData.getModule() : "-");
            params.addValue("action", sessionData.getAction() != null ? sessionData.getAction() : "-");
            params.addValue("logonTime", sessionData.getLogonTime());
            params.addValue("lastCallEt", sessionData.getLastCallEt() != null ? sessionData.getLastCallEt() : 0);
            params.addValue("failedOver", sessionData.getFailedOver() != null ? sessionData.getFailedOver() : "-");
            params.addValue("blockingSessionStatus", sessionData.getBlockingSessionStatus() != null ? sessionData.getBlockingSessionStatus() : "-");
            params.addValue("event", sessionData.getEvent() != null ? sessionData.getEvent() : "-");
            params.addValue("waitClass", sessionData.getWaitClass() != null ? sessionData.getWaitClass() : "-");
            params.addValue("state", sessionData.getState() != null ? sessionData.getState() : "-");
            params.addValue("waitTimeMicro", sessionData.getWaitTimeMicro() != null ? sessionData.getWaitTimeMicro() : 0);
            params.addValue("timeRemainingMicro", sessionData.getTimeRemainingMicro() != null ? sessionData.getTimeRemainingMicro() : 0);
            params.addValue("serviceName", sessionData.getServiceName() != null ? sessionData.getServiceName() : "-");
        });
    }

}
