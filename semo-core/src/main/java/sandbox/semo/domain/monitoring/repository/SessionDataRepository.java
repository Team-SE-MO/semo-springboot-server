package sandbox.semo.domain.monitoring.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.monitoring.entity.SessionDataId;

public interface SessionDataRepository extends JpaRepository<SessionData, SessionDataId> {

    @Query("""
            SELECT s
            FROM SessionData s
            WHERE s.id.deviceId = :deviceId
              AND s.id.collectedAt = :collectedAt
            ORDER BY s.id.sid DESC
            """)
    Page<SessionData> findSessionData(
            @Param("deviceId") Long deviceId,
            @Param("collectedAt") LocalDateTime collectedAt,
            Pageable pageable
    );

}
