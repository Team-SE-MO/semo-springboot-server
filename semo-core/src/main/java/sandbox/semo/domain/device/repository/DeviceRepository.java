package sandbox.semo.domain.device.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.device.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    @Query("SELECT d " +
            "FROM Device d " +
            "WHERE d.deviceAlias = :deviceAlias " +
            "AND d.company.id = :companyId")
    Optional<Device> findByAliasAndCompanyId(
            @Param("deviceAlias") String deviceAlias,
            @Param("companyId") Long companyId
    );

    @Query("SELECT d.id " +
            "FROM Device d " +
            "WHERE d.deviceAlias = :deviceAlias " +
            "AND d.company.id = :companyId")
    Long findIdByAliasAndCompanyId(
            @Param("deviceAlias") String deviceAlias,
            @Param("companyId") Long companyId
    );

    @Query(value = """
                SELECT * FROM DEVICES
                WHERE (
                    (:includeCompanyId = 1 AND COMPANY_ID = :companyId)
                    OR (:includeCompanyId = 0 AND (:companyId IS NULL OR COMPANY_ID = :companyId))
                )
                AND DELETED_AT IS NULL
                ORDER BY UPDATED_AT DESC
                OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
            """, nativeQuery = true)
    List<Device> findDevicesWithOffset(
            @Param("includeCompanyId") int includeCompanyId,
            @Param("companyId") Long companyId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Query(value = """
                SELECT COUNT(*) FROM DEVICES
                WHERE (
                    (:includeCompanyId = 1 AND COMPANY_ID = :companyId)
                    OR (:includeCompanyId = 0 AND (:companyId IS NULL OR COMPANY_ID = :companyId))
                )
                AND DELETED_AT IS NULL
            """, nativeQuery = true)
    long countDevices(
            @Param("includeCompanyId") int includeCompanyId,
            @Param("companyId") Long companyId
    );

    @Query(value = """
            SELECT \
                d.DEVICE_ALIAS, d.TYPE, d.IP, d.PORT, d.SID, \
                CASE \
                    WHEN COALESCE(m.BLOCKING_SESSION_COUNT, 0) >= 3 THEN 'BLOCKED' \
                    WHEN d.STATUS = 1 THEN 'ACTIVE' \
                    ELSE 'INACTIVE' \
                END AS status, \
                CASE \
                    WHEN COALESCE(m.BLOCKING_SESSION_COUNT, 0) >= 3 THEN m.BLOCKING_SESSION_COUNT \
                    WHEN d.STATUS = 0 THEN EXTRACT(MINUTE FROM (SYSDATE - MAX(m.COLLECTED_AT))) \
                    ELSE COUNT(s.DEVICE_ID) \
                END AS statusValue, \
                MAX(m.COLLECTED_AT) AS lastCollectedAt \
            FROM DEVICES d
            LEFT JOIN MONITORING_METRICS m ON d.DEVICE_ID = m.DEVICE_ID \
                AND m.COLLECTED_AT = (SELECT MAX(m2.COLLECTED_AT)
                                      FROM monitoring_metrics m2
                                      WHERE m2.DEVICE_ID = d.DEVICE_ID)
            LEFT JOIN SESSION_DATA s ON d.DEVICE_ID = s.DEVICE_ID
                AND (s.COLLECTED_AT, s.DEVICE_ID) IN (SELECT MAX(s2.COLLECTED_AT), s2.DEVICE_ID \
                                                      FROM SESSION_DATA s2
                                                      GROUP BY s2.DEVICE_ID)
            WHERE d.COMPANY_ID = :companyId
            GROUP BY d.DEVICE_ALIAS, d.TYPE, d.IP, d.PORT, d.SID, d.STATUS, m.BLOCKING_SESSION_COUNT
            ORDER BY status DESC, statusValue DESC
            """, nativeQuery = true)
    List<Object[]> findMetricSummaryDataByCompanyId(@Param("companyId") Long companyId);

}
