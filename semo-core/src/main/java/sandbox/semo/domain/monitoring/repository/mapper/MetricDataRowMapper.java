package sandbox.semo.domain.monitoring.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.entity.MonitoringMetricId;
import sandbox.semo.domain.device.entity.Device;

@RequiredArgsConstructor
public class MetricDataRowMapper implements RowMapper<MonitoringMetric> {

    private final Device device;
    private final LocalDateTime collectedAt;

    @Override
    public MonitoringMetric mapRow(ResultSet rs, int rowNum) throws SQLException {
        MonitoringMetricId id = MonitoringMetricId.builder()
                .collectedAt(collectedAt)
                .deviceId(device.getId())
                .build();

        return MonitoringMetric.builder()
                .id(id)
                .device(device)
                .totalSessionCount(rs.getInt("TOTAL_SESSION_COUNT"))
                .activeSessionCount(rs.getInt("ACTIVE_SESSION_COUNT"))
                .sessionCountGroupByUser(rs.getString("SESSION_COUNT_GROUP_BY_USER"))
                .sessionCountGroupByCommand(rs.getString("SESSION_COUNT_GROUP_BY_COMMAND"))
                .sessionCountGroupByMachine(rs.getString("SESSION_COUNT_GROUP_BY_MACHINE"))
                .sessionCountGroupByType(rs.getString("SESSION_COUNT_GROUP_BY_TYPE"))
                .blockingSessionCount(rs.getInt("BLOCKING_SESSION_COUNT"))
                .waitSessionCount(rs.getInt("WAIT_SESSION_COUNT"))
                .build();
    }

}
