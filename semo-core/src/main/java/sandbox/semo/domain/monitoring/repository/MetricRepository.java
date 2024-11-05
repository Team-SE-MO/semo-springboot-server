package sandbox.semo.domain.monitoring.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;

public interface MetricRepository extends JpaRepository<MonitoringMetric, Long> {

    @Query("SELECT m " +
            "FROM MonitoringMetric m " +
            "WHERE m.id.collectedAt BETWEEN :startTime AND :endTime " +
            "AND m.device.id = :deviceId " +
            "ORDER BY m.id.collectedAt")
    List<MonitoringMetric> findMetricsByTimeRangeAndDeviceId(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("deviceId") Long deviceId
    );

}
