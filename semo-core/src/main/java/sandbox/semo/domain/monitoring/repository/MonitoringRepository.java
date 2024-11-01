package sandbox.semo.domain.monitoring.repository;

import static sandbox.semo.domain.common.util.ParameterSourceConverter.convertToSqlParameterSource;
import static sandbox.semo.domain.common.util.ParameterSourceConverter.convertToSqlParameterSourceArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import sandbox.semo.domain.common.config.QueryLoader;
import sandbox.semo.domain.monitoring.dto.response.MetricSummaryData;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.monitoring.repository.mapper.MetricDataRowMapper;
import sandbox.semo.domain.monitoring.repository.mapper.MetricSummaryDataRowMapper;
import sandbox.semo.domain.monitoring.repository.mapper.SessionDataRowMapper;

@Log4j2
@Repository
@RequiredArgsConstructor
public class MonitoringRepository {

    private final QueryLoader queryLoader;
    private final NamedParameterJdbcTemplate paramJdbcTemplate;

    public void deviceStatusUpdate(boolean status, Long deviceId) {
        String query = queryLoader.getQuery("updateDeviceStatus");
        MapSqlParameterSource params = convertToSqlParameterSource(status, deviceId);
        paramJdbcTemplate.update(query, params);
    }

    public List<SessionData> fetchSessionData(DataSource dataSource, Device device, LocalDateTime collectedAt) {
        List<SessionData> sessionDataList = new ArrayList<>();
        String query = queryLoader.getQuery("selectSessionData");

        log.info(">>> [ 🔍 SessionData 조회 시작: Device {} ]", device.getDeviceAlias());
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query))
        {
            ResultSet rs = stmt.executeQuery();
            SessionDataRowMapper rowMapper = new SessionDataRowMapper(device, collectedAt);
            while (rs.next()) {
                SessionData sessionData = rowMapper.mapRow(rs, rs.getRow());
                sessionDataList.add(sessionData);
            }
            log.info(">>> [ 📊 SessionData 조회 완료: Device {}. 조회된 데이터 개수: {} ]",
                    device.getDeviceAlias(), sessionDataList.size()
            );
        } catch (SQLException e) {
            log.error(">>> [ ❌ SessionData 조회 실패: Device {}. 에러: {} ]",
                    device.getDeviceAlias(), e.getMessage());
        }
        return sessionDataList;
    }

    public void saveSessionData(List<SessionData> sessionDataList) {
        String query = queryLoader.getQuery("insertSessionData");
        Instant start = Instant.now();
        log.info(">>> [ 💾 SessionData 저장 시작. 데이터 개수: {} ]", sessionDataList.size());

        try {
            SqlParameterSource[] batchParams = convertToSqlParameterSourceArray(sessionDataList);
            paramJdbcTemplate.batchUpdate(query, batchParams);
            Instant end = Instant.now();
            log.info(">>> [ ✅ SessionData 저장 완료 - 소요 시간: {}ms ]", end.toEpochMilli() - start.toEpochMilli());
        } catch (Exception e) {
            log.error(">>> [ ❌ SessionData 저장 실패: 에러: {} ]", e.getMessage(), e);
        }
    }

    public MonitoringMetric fetchMetricData(DataSource dataSource, Device device, LocalDateTime collectedAt) {
        MonitoringMetric monitoringMetric = null;
        String query = queryLoader.getQuery("selectMetricData");

        log.info(">>> [ 🔍 MetricData 조회 시작: Device {} ]", device.getDeviceAlias());
        try {
            monitoringMetric = new JdbcTemplate(dataSource).queryForObject(
                    query, new MetricDataRowMapper(device, collectedAt)
            );
            log.info(">>> [ 📊 MetricData 조회 완료: Device {} ]", device.getDeviceAlias());
        } catch (Exception e) {
            log.error(">>> [ ❌ MetricData 조회 실패: Device {}. 에러: {} ]", device.getDeviceAlias(), e.getMessage());
        }
        return monitoringMetric;
    }

    public void saveMonitoringMetric(MonitoringMetric monitoringMetric) {
        String query = queryLoader.getQuery("insertMonitoringMetric");
        Instant start = Instant.now();

        log.info(">>> [ 💾 MonitoringMetric 저장 시작: Device {} ]", monitoringMetric.getDevice().getDeviceAlias());
        try {
            MapSqlParameterSource params = convertToSqlParameterSource(monitoringMetric);
            paramJdbcTemplate.update(query, params);
            Instant end = Instant.now();
            log.info(">>> [ ✅ MonitoringMetric 저장 완료 - 소요 시간: {}ms ]", end.toEpochMilli() - start.toEpochMilli());
        } catch (Exception e) {
            log.error(">>> [ ❌ MonitoringMetric 저장 실패: Device {}. 에러: {} ]", monitoringMetric.getDevice().getDeviceAlias(), e.getMessage());
        }
    }

    public MetricSummaryData fetchMetricSummaryData(Long companyId) {
        String query = queryLoader.getQuery("selectMetricSummaryData");
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("companyId", companyId);
        return paramJdbcTemplate.queryForObject(
                query, params, new MetricSummaryDataRowMapper()
        );
    }

}
