package sandbox.semo.domain.monitoring.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;
import sandbox.semo.domain.monitoring.dto.request.CsvFileData;

public class CsvFileDataRowMapper implements RowMapper<CsvFileData> {

    @Override
    public CsvFileData mapRow(ResultSet rs, int rowNum) throws SQLException {
        CsvFileData data = new CsvFileData();
        data.setCollectedAt(getTimestamp(rs, "COLLECTED_AT"));
        data.setSid(getLong(rs, "SID"));
        data.setDeviceId(getLong(rs, "DEVICE_ID"));
        data.setSerial(getLong(rs, "SERIAL"));
        data.setUsername(rs.getString("USERNAME"));
        data.setCommand(getLong(rs, "COMMAND"));
        data.setCommandName(rs.getString("COMMAND_NAME"));
        data.setStatus(rs.getString("STATUS"));
        data.setSchemaName(rs.getString("SCHEMA_NAME"));
        data.setOsUser(rs.getString("OS_USER"));
        data.setProcess(rs.getString("PROCESS"));
        data.setMachine(rs.getString("MACHINE"));
        data.setPort(getInt(rs, "PORT"));
        data.setTerminal(rs.getString("TERMINAL"));
        data.setProgram(rs.getString("PROGRAM"));
        data.setType(rs.getString("TYPE"));
        data.setSqlId(rs.getString("SQL_ID"));
        data.setSqlExecStart(getTimestamp(rs, "SQL_EXEC_START"));
        data.setSqlExecId(getLong(rs, "SQL_EXEC_ID"));
        data.setSqlText(rs.getString("SQL_TEXT"));
        data.setModule(rs.getString("MODULE"));
        data.setAction(rs.getString("ACTION"));
        data.setLogonTime(getTimestamp(rs, "LOGON_TIME"));
        data.setLastCallEt(getLong(rs, "LAST_CALL_ET"));
        data.setFailedOver(rs.getString("FAILED_OVER"));
        data.setBlockingSessionStatus(rs.getString("BLOCKING_SESSION_STATUS"));
        data.setEvent(rs.getString("EVENT"));
        data.setWaitClass(rs.getString("WAIT_CLASS"));
        data.setState(rs.getString("STATE"));
        data.setWaitTimeMicro(getLong(rs, "WAIT_TIME_MICRO"));
        data.setTimeRemainingMicro(getLong(rs, "TIME_REMAINING_MICRO"));
        data.setServiceName(rs.getString("SERVICE_NAME"));
        data.setCompanyId(getLong(rs, "COMPANY_ID"));

        return data;
    }
    
    private LocalDateTime getTimestamp(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
    
    private Long getLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }
    
    private Integer getInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}
