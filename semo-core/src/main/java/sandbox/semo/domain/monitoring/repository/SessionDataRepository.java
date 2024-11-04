package sandbox.semo.domain.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.domain.monitoring.entity.SessionData;

public interface SessionDataRepository extends JpaRepository<SessionData, Long> {
}
