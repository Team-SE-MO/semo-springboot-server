package sandbox.semo.domain.monitoring.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import sandbox.semo.domain.monitoring.dto.response.MetricSummaryData;

public class MetricSummaryDataRowMapper implements RowMapper<MetricSummaryData> {

    @Override
    public MetricSummaryData mapRow(ResultSet rs, int rowNum) throws SQLException {
        return MetricSummaryData.builder()
                .activeDeviceCount(rs.getInt("ACTIVE_DEVICE_COUNT"))
                .inactiveDeviceCount(rs.getInt("INACTIVE_DEVICE_COUNT"))
                .blockedDeviceCount(rs.getInt("BLOCKED_DEVICE_COUNT"))
                .topUsedDevices(rs.getString("TOP_USED_DEVICE_COUNT"))
                .unusedDevices(rs.getString("UNUSED_DEVICE_COUNT"))
                .allDevices(rs.getString("ALL_DEVICES"))
                .build();
    }
}
