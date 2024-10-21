package sandbox.semo.application.device.service;

import static sandbox.semo.application.device.exception.DeviceErrorCode.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import sandbox.semo.application.common.util.AES256;
import sandbox.semo.application.device.exception.DeviceBusinessException;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.device.dto.request.DeviceRegister;
import sandbox.semo.domain.device.dto.request.DataBaseInfo;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.device.repository.DeviceRepository;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public void register(Company company, DeviceRegister request) {
        DataBaseInfo dataBaseInfo = request.getDataBaseInfo();
        deviceRepository.save(Device.builder()
                .company(company)
                .deviceAlias(request.getDeviceAlias())
                .type(dataBaseInfo.getType())
                .ip(dataBaseInfo.getIp())
                .port(dataBaseInfo.getPort())
                .sid(dataBaseInfo.getSid())
                .username(dataBaseInfo.getUsername())
                .password(AES256.encrypt(dataBaseInfo.getPassword()))
                .status(healthCheck(dataBaseInfo))
                .build());
        log.info(">>> [ ✅ 데이터베이스 장비가 성공적으로 등록되었습니다. ]");
    }

    @Override
    public boolean healthCheck(DataBaseInfo request) {
        try (Connection conn = getDBConnection(request)) {
            validateDBConnection(conn);
            checkVSessionAccess(conn);
            return true;
        } catch (SQLException e) {
            return false;
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

    private Connection getDBConnection(DataBaseInfo request) throws SQLException {
        String url = getConnectionUrl(request.getIp(), request.getPort(), request.getSid());
        return DriverManager.getConnection(url, request.getUsername(), request.getPassword());
    }

    private String getConnectionUrl(String ip, Long port, String sId) {
        return "jdbc:oracle:thin:@" + ip + ":" + port + ":" + sId;
    }

}
