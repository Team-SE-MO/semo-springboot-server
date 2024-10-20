package sandbox.semo.application.device.service;

import static sandbox.semo.application.device.exception.DeviceErrorCode.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import sandbox.semo.application.device.exception.DeviceBusinessException;
import sandbox.semo.domain.device.dto.request.HealthCheck;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    @Override
    public void healthCheck(HealthCheck request) {
        try (Connection conn = getDBConnection(request)) {
            validateDBConnection(conn);
            checkVSessionAccess(conn);
        } catch (SQLException e) {
            throw new DeviceBusinessException(DATABASE_CONNECTION_FAILURE);
        }
    }

    private void checkVSessionAccess(Connection conn) throws SQLException {
        String checkQuery = "SELECT 1 FROM v$session WHERE ROWNUM = 1";

        try (
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(checkQuery)
        ) {
            if (!rs.next()) {
                log.warn(">>> [ ❌ v$session 테이블에 접근이 불가능 합니다. ]");
                throw new DeviceBusinessException(ACCESS_DENIED);
            }
        }
        log.info(">>> [ ✅ v$session 테이블 접근 가능 ]");
    }

    private void validateDBConnection(Connection conn) throws SQLException {
        if (!conn.isValid(2)) {
            log.warn(">>> [ ❌ 데이터베이스 연결 실패: 연결 시도 시간 초과 ]");
            throw new DeviceBusinessException(DATABASE_CONNECTION_FAILURE);
        }
        log.info(">>> [ ✅ 데이터베이스 연결 체크 성공 ]");
    }

    private Connection getDBConnection(HealthCheck request) throws SQLException {
        String url = getConnectionUrl(request.getIp(), request.getPort(), request.getSid());
        return DriverManager.getConnection(url, request.getUsername(), request.getPassword());
    }

    private String getConnectionUrl(String ip, Long port, String sId) {
        return "jdbc:oracle:thin:@" + ip + ":" + port + ":" + sId;
    }

}
