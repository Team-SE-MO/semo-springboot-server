package sandbox.semo.batch.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import sandbox.semo.domain.collection.entity.SessionData;
import sandbox.semo.domain.collection.entity.SessionDataId;
import sandbox.semo.domain.device.entity.Device;

@RequiredArgsConstructor
public class SessionDataRowMapper implements RowMapper<SessionData> {

    private final Device device;
    private final LocalDateTime collectedAt;

    @Override
    public SessionData mapRow(ResultSet rs, int rowNum) throws SQLException {
        SessionDataId id = SessionDataId.builder()
                .collectedAt(collectedAt)
                .sid(rs.getLong("SID"))
                .deviceId(device.getId())
                .build();

        LocalDateTime sqlExecStart =
                rs.getTimestamp("SQL_EXEC_START") != null ?
                rs.getTimestamp("SQL_EXEC_START").toLocalDateTime() : null;

        LocalDateTime logonTime =
                rs.getTimestamp("LOGON_TIME") != null ?
                rs.getTimestamp("LOGON_TIME").toLocalDateTime() : null;

        return SessionData.builder()
                .id(id)
                .device(device)
                .serial(rs.getObject("SERIAL#", Long.class))
                .username(rs.getString("USERNAME"))
                .command(rs.getObject("COMMAND", Long.class))
                .commandName(rs.getString("COMMAND_NAME"))
                .status(rs.getString("STATUS"))
                .schemaName(rs.getString("SCHEMANAME"))
                .osUser(rs.getString("OSUSER"))
                .process(rs.getString("PROCESS"))
                .machine(rs.getString("MACHINE"))
                .port(rs.getObject("PORT", Integer.class))
                .terminal(rs.getString("TERMINAL"))
                .program(rs.getString("PROGRAM"))
                .type(rs.getString("TYPE"))
                .sqlId(rs.getString("SQL_ID"))
                .sqlExecStart(sqlExecStart)
                .sqlExecId(rs.getObject("SQL_EXEC_ID", Long.class))
                .sqlText(rs.getString("SQL_TEXT"))
                .module(rs.getString("MODULE"))
                .action(rs.getString("ACTION"))
                .logonTime(logonTime)
                .lastCallEt(rs.getObject("LAST_CALL_ET", Long.class))
                .failedOver(rs.getString("FAILED_OVER"))
                .blockingSessionStatus(rs.getString("BLOCKING_SESSION_STATUS"))
                .event(rs.getString("EVENT"))
                .waitClass(rs.getString("WAIT_CLASS"))
                .state(rs.getString("STATE"))
                .waitTimeMicro(rs.getObject("WAIT_TIME_MICRO", Long.class))
                .timeRemainingMicro(rs.getObject("TIME_REMAINING_MICRO", Long.class))
                .serviceName(rs.getString("SERVICE_NAME"))
                .build();
    }

}
