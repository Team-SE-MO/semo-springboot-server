package sandbox.semo.batch.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import sandbox.semo.batch.config.QueryLoader;
import sandbox.semo.batch.repository.mapper.SessionDataRowMapper;
import sandbox.semo.domain.collection.entity.SessionData;
import sandbox.semo.domain.device.entity.Device;

@Log4j2
@Repository
@RequiredArgsConstructor
public class JdbcRepository {

    private final QueryLoader queryLoader;
    private final JdbcTemplate jdbcTemplate;

    public void deviceStatusUpdate(boolean status, Long deviceId) {
        String query = queryLoader.getQuery("updateDeviceStatus");
        jdbcTemplate.update(query, status, deviceId);
    }

    public List<SessionData> fetchSessionData(DataSource dataSource, Device device, LocalDateTime collectedAt) {
        List<SessionData> sessionDataList = new ArrayList<>();
        String query = queryLoader.getQuery("selectSessionData");

        log.info(">>> [ 🔍 SessionData 조회 시작: Device {} ]", device.getDeviceAlias());
        try (Connection conn = dataSource.getConnection();
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
        log.info(">>> [ 💾 SessionData 저장 시작. 데이터 개수: {} ]", sessionDataList.size());
        sessionDataList.forEach(sessionData -> {
            try {
                jdbcTemplate.update(query,
                        sessionData.getId().getCollectedAt(),
                        sessionData.getId().getSid(),
                        sessionData.getId().getDeviceId(),
                        sessionData.getSerial() != null ? sessionData.getSerial() : 0,
                        sessionData.getUsername() != null ? sessionData.getUsername() : "-",
                        sessionData.getCommand() != null ? sessionData.getCommand() : 0,
                        sessionData.getCommandName() != null ? sessionData.getCommandName() : "-",
                        sessionData.getStatus() != null ? sessionData.getStatus() : "-",
                        sessionData.getSchemaName() != null ? sessionData.getSchemaName() : "-",
                        sessionData.getOsUser() != null ? sessionData.getOsUser() : "-",
                        sessionData.getProcess() != null ? sessionData.getProcess() : "-",
                        sessionData.getMachine() != null ? sessionData.getMachine() : "-",
                        sessionData.getPort() != null ? sessionData.getPort() : -1,
                        sessionData.getTerminal() != null ? sessionData.getTerminal() : "-",
                        sessionData.getProgram() != null ? sessionData.getProgram() : "-",
                        sessionData.getType() != null ? sessionData.getType() : "-",
                        sessionData.getSqlId() != null ? sessionData.getSqlId() : "-",
                        sessionData.getSqlExecStart() != null ? sessionData.getSqlExecStart() : null,
                        sessionData.getSqlExecId() != null ? sessionData.getSqlExecId() : 0,
                        sessionData.getSqlText() != null ? sessionData.getSqlText() : "-",
                        sessionData.getModule() != null ? sessionData.getModule() : "-",
                        sessionData.getAction() != null ? sessionData.getAction() : "-",
                        sessionData.getLogonTime() != null ? sessionData.getLogonTime() : null,
                        sessionData.getLastCallEt() != null ? sessionData.getLastCallEt() : 0,
                        sessionData.getFailedOver() != null ? sessionData.getFailedOver() : "-",
                        sessionData.getBlockingSessionStatus() != null ? sessionData.getBlockingSessionStatus() : "-",
                        sessionData.getEvent() != null ? sessionData.getEvent() : "-",
                        sessionData.getWaitClass() != null ? sessionData.getWaitClass() : "-",
                        sessionData.getState() != null ? sessionData.getState() : "-",
                        sessionData.getWaitTimeMicro() != null ? sessionData.getWaitTimeMicro() : 0,
                        sessionData.getTimeRemainingMicro() != null ? sessionData.getTimeRemainingMicro() : 0,
                        sessionData.getServiceName() != null ? sessionData.getServiceName() : "-"
                );
            } catch (Exception e) {
                log.error(">>> [ ❌ SessionData 저장 실패: SID {}. 에러: {} ]",
                        sessionData.getId().getSid(), e.getMessage());
            }
        });
        log.info(">>> [ ✅ SessionData 저장 완료 ]");
    }
}
