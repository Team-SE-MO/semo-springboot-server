package sandbox.semo.domain.monitoring.repository;

import static sandbox.semo.domain.common.util.ParameterSourceConverter.convertToSqlParameterSource;

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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import sandbox.semo.domain.common.config.QueryLoader;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.monitoring.repository.mapper.MetricDataRowMapper;
import sandbox.semo.domain.monitoring.repository.mapper.SessionDataRowMapper;

@Log4j2
@Repository
@RequiredArgsConstructor
public class MonitoringRepository {

    private final QueryLoader queryLoader;
    private final NamedParameterJdbcTemplate paramJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public void deviceStatusUpdate(boolean status, Long deviceId) {
        String query = queryLoader.getQuery("updateDeviceStatus");
        MapSqlParameterSource params = convertToSqlParameterSource(status, deviceId);
        paramJdbcTemplate.update(query, params);
    }

    public List<SessionData> fetchSessionData(DataSource dataSource, Device device,
        LocalDateTime collectedAt) {
        List<SessionData> sessionDataList = new ArrayList<>();
        String query = queryLoader.getQuery("selectSessionData");

        log.info(">>> [ 🔍 SessionData 조회 시작: Device {} ]", device.getDeviceAlias());
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
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
            jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    setSessionDataValues(ps, sessionDataList.get(i));
                }

                @Override
                public int getBatchSize() {
                    return sessionDataList.size();
                }
            });
            Instant end = Instant.now();
            log.info(">>> [ ✅ SessionData 저장 완료 - 소요 시간: {}ms ]",
                end.toEpochMilli() - start.toEpochMilli());
        } catch (Exception e) {
            log.error(">>> [ ❌ SessionData 저장 실패: 에러: {} ]", e.getMessage(), e);
        }
    }

    public void deleteExpiredSessionDataList(
        LocalDateTime retentionDate) {
        String query = queryLoader.getQuery("deleteExpiredSessionDataList");
        log.info(">>> [ 💾 SessionData 삭제 시작.]");

        try {
            // 단일 쿼리로 변경
            int deletedCount = jdbcTemplate.update(query, retentionDate);

            log.info(">>> [ 🗑️ CollectedAt: {} 기준 {} 개 행 삭제됨 ]",
                retentionDate,
                deletedCount);

        } catch (Exception e) {
            log.error(">>> [ ❌ SessionData 삭제 실패: 에러: {} ]", e.getMessage(), e);
            throw e;
        }
    }

    public MonitoringMetric fetchMetricData(DataSource dataSource, Device device,
        LocalDateTime collectedAt) {
        MonitoringMetric monitoringMetric = null;
        String query = queryLoader.getQuery("selectMetricData");

        log.info(">>> [ 🔍 MetricData 조회 시작: Device {} ]", device.getDeviceAlias());
        try {
            monitoringMetric = new JdbcTemplate(dataSource).queryForObject(
                query, new MetricDataRowMapper(device, collectedAt)
            );
            log.info(">>> [ 📊 MetricData 조회 완료: Device {} ]", device.getDeviceAlias());
        } catch (Exception e) {
            log.error(">>> [ ❌ MetricData 조회 실패: Device {}. 에러: {} ]", device.getDeviceAlias(),
                e.getMessage());
        }
        return monitoringMetric;
    }

    public void saveMonitoringMetric(MonitoringMetric monitoringMetric) {
        String query = queryLoader.getQuery("insertMonitoringMetric");
        Instant start = Instant.now();

        log.info(">>> [ 💾 MonitoringMetric 저장 시작: Device {} ]",
            monitoringMetric.getDevice().getDeviceAlias());
        try {
            MapSqlParameterSource params = convertToSqlParameterSource(monitoringMetric);
            paramJdbcTemplate.update(query, params);
            Instant end = Instant.now();
            log.info(">>> [ ✅ MonitoringMetric 저장 완료 - 소요 시간: {}ms ]",
                end.toEpochMilli() - start.toEpochMilli());
        } catch (Exception e) {
            log.error(">>> [ ❌ MonitoringMetric 저장 실패: Device {}. 에러: {} ]",
                monitoringMetric.getDevice().getDeviceAlias(), e.getMessage());
        }
    }

    private void setSessionDataValues(PreparedStatement ps, SessionData sessionData)
        throws SQLException {
        ps.setObject(1, sessionData.getId().getCollectedAt());
        ps.setLong(2, sessionData.getId().getSid());
        ps.setLong(3, sessionData.getId().getDeviceId());
        ps.setLong(4, sessionData.getSerial() != null ? sessionData.getSerial() : 0);
        ps.setString(5, sessionData.getUsername() != null ? sessionData.getUsername() : "-");
        ps.setLong(6, sessionData.getCommand() != null ? sessionData.getCommand() : 0);
        ps.setString(7, sessionData.getCommandName() != null ? sessionData.getCommandName() : "-");
        ps.setString(8, sessionData.getStatus() != null ? sessionData.getStatus() : "-");
        ps.setString(9, sessionData.getSchemaName() != null ? sessionData.getSchemaName() : "-");
        ps.setString(10, sessionData.getOsUser() != null ? sessionData.getOsUser() : "-");
        ps.setString(11, sessionData.getProcess() != null ? sessionData.getProcess() : "-");
        ps.setString(12, sessionData.getMachine() != null ? sessionData.getMachine() : "-");
        ps.setInt(13, sessionData.getPort() != null ? sessionData.getPort() : -1);
        ps.setString(14, sessionData.getTerminal() != null ? sessionData.getTerminal() : "-");
        ps.setString(15, sessionData.getProgram() != null ? sessionData.getProgram() : "-");
        ps.setString(16, sessionData.getType() != null ? sessionData.getType() : "-");
        ps.setString(17, sessionData.getSqlId() != null ? sessionData.getSqlId() : "-");
        ps.setObject(18, sessionData.getSqlExecStart());
        ps.setLong(19, sessionData.getSqlExecId() != null ? sessionData.getSqlExecId() : 0);
        ps.setString(20, sessionData.getSqlText() != null ? sessionData.getSqlText() : "-");
        ps.setString(21, sessionData.getModule() != null ? sessionData.getModule() : "-");
        ps.setString(22, sessionData.getAction() != null ? sessionData.getAction() : "-");
        ps.setObject(23, sessionData.getLogonTime());
        ps.setLong(24, sessionData.getLastCallEt() != null ? sessionData.getLastCallEt() : 0);
        ps.setString(25, sessionData.getFailedOver() != null ? sessionData.getFailedOver() : "-");
        ps.setString(26,
            sessionData.getBlockingSessionStatus() != null ? sessionData.getBlockingSessionStatus()
                : "-");
        ps.setString(27, sessionData.getEvent() != null ? sessionData.getEvent() : "-");
        ps.setString(28, sessionData.getWaitClass() != null ? sessionData.getWaitClass() : "-");
        ps.setString(29, sessionData.getState() != null ? sessionData.getState() : "-");
        ps.setLong(30, sessionData.getWaitTimeMicro() != null ? sessionData.getWaitTimeMicro() : 0);
        ps.setLong(31,
            sessionData.getTimeRemainingMicro() != null ? sessionData.getTimeRemainingMicro() : 0);
        ps.setString(32, sessionData.getServiceName() != null ? sessionData.getServiceName() : "-");
    }
}
