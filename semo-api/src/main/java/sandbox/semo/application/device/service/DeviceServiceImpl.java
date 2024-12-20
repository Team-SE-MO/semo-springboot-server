package sandbox.semo.application.device.service;

import static sandbox.semo.application.common.exception.CommonErrorCode.BAD_REQUEST;
import static sandbox.semo.application.common.exception.CommonErrorCode.FORBIDDEN_ACCESS;
import static sandbox.semo.application.company.exception.CompanyErrorCode.*;
import static sandbox.semo.application.device.exception.DeviceErrorCode.ACCESS_DENIED;
import static sandbox.semo.application.device.exception.DeviceErrorCode.DATABASE_CONNECTION_FAILURE;
import static sandbox.semo.application.device.exception.DeviceErrorCode.DEVICE_NOT_FOUND;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.common.exception.CommonBusinessException;
import sandbox.semo.application.company.exception.CompanyBusinessException;
import sandbox.semo.application.device.exception.DeviceBusinessException;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.common.dto.response.OffsetPage;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.device.dto.request.DataBaseInfo;
import sandbox.semo.domain.device.dto.request.DeviceRegister;
import sandbox.semo.domain.device.dto.request.DeviceUpdate;
import sandbox.semo.domain.device.dto.response.DeviceInfo;
import sandbox.semo.domain.device.dto.response.DeviceInfoWithCompanyInfo;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.device.repository.DeviceRepository;
import sandbox.semo.domain.member.entity.Role;

@Service
@Log4j2
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final CompanyRepository companyRepository;
    private final AES256 aes256;

    @Override
    public OffsetPage<?> findDevices(int page, int size, Role role, Long companyId) {
        if (page < 1) {
            throw new CommonBusinessException(BAD_REQUEST);
        }
        int offset = (page - 1) * size;
        int includeCompanyId = switch (role) {
            case ROLE_SUPER -> 0;
            case ROLE_ADMIN -> 1;
            default -> throw new CommonBusinessException(FORBIDDEN_ACCESS);
        };

        List<Device> devices = deviceRepository.findDevicesWithOffset(includeCompanyId, companyId, offset, size);
        int totalCount = (int) deviceRepository.countDevices(includeCompanyId, companyId);

        List<?> content = devices.stream()
                .map(role == Role.ROLE_SUPER ?
                        this::mapToDeviceInfoWithCompanyInfo :
                        this::mapToDeviceInfo
                ).toList();
        int pageCount = (int) Math.ceil((double) totalCount / size);
        boolean hasNext = (page * size) < totalCount;

        return new OffsetPage<>(pageCount, content, hasNext);
    }

    private DeviceInfo mapToDeviceInfo(Device device) {
        return DeviceInfo.builder()
                .deviceAlias(device.getDeviceAlias())
                .type(device.getType())
                .ip(device.getIp())
                .port(device.getPort())
                .sid(device.getSid())
                .status(device.getStatus())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }

    private DeviceInfoWithCompanyInfo mapToDeviceInfoWithCompanyInfo(Device device) {
        return new DeviceInfoWithCompanyInfo(
                device.getCompany().getCompanyName(),
                device.getCompany().getTaxId(),
                device.getDeviceAlias(),
                device.getType(),
                device.getIp(),
                device.getPort(),
                device.getSid(),
                device.getStatus(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }

    @Transactional
    @Override
    public void register(Long companyId, DeviceRegister request) {
        DataBaseInfo dataBaseInfo = request.getDataBaseInfo();
        if (!healthCheck(dataBaseInfo)) {
            throw new DeviceBusinessException(DATABASE_CONNECTION_FAILURE);
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyBusinessException(COMPANY_NOT_FOUND));
        deviceRepository.save(Device.builder()
                .company(company)
                .deviceAlias(request.getDeviceAlias())
                .type(dataBaseInfo.getType())
                .ip(dataBaseInfo.getIp())
                .port(dataBaseInfo.getPort())
                .sid(dataBaseInfo.getSid())
                .username(dataBaseInfo.getUsername())
                .password(aes256.encrypt(dataBaseInfo.getPassword()))
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

    @Transactional
    @Override
    public void update(Long companyId, DeviceUpdate request) {
        Device device = deviceRepository.findByAliasAndCompanyId(
                request.getTargetDevice(),
                companyId
        ).orElseThrow(() -> new DeviceBusinessException(DEVICE_NOT_FOUND));

        DataBaseInfo dataBaseInfo = request.getUpdateDeviceInfo();
        if (!healthCheck(dataBaseInfo)) {
            throw new DeviceBusinessException(DATABASE_CONNECTION_FAILURE);
        }

        device.changeDevice(
                request.getUpdateDeviceAlias(),
                dataBaseInfo.getType(),
                dataBaseInfo.getIp(),
                dataBaseInfo.getPort(),
                dataBaseInfo.getSid(),
                dataBaseInfo.getUsername(),
                aes256.encrypt(dataBaseInfo.getPassword())
        );
        log.info(">>> [ ✅ 데이터베이스 장비가 성공적으로 수정되었습니다. ]");
    }

    @Transactional
    @Override
    public void deleteDevice(Long companyId, String deviceAlias) {
        Device device = deviceRepository.findByAliasAndCompanyId(deviceAlias, companyId)
                .orElseThrow(() -> new DeviceBusinessException(DEVICE_NOT_FOUND));
        device.markAsDeleted();
        log.info(">>> [ ✅ 데이터베이스 장비가 성공적으로 삭제 되었습니다. ]");
    }
}
